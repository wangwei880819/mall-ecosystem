package com.igou.mall.controller;

import com.igou.mall.common.Result;
import com.igou.mall.service.DeepSeekService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.*;
import java.nio.file.*;
import java.util.*;

@RestController
@RequestMapping("/api/ocr")
@CrossOrigin(origins = "*")
public class OcrController {

    private static final Logger log = LoggerFactory.getLogger(OcrController.class);

    @Value("${file.upload-dir:#{systemProperties['user.dir'] + '/uploads'}}")
    private String uploadDir;

    @Autowired
    private DeepSeekService deepSeekService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping("/license")
    public Result<Map<String, Object>> recognizeLicense(@RequestParam("file") MultipartFile file) {
        try {
            String imagePath = saveTempFile(file);
            // 图片预处理：缩放+灰度+自适应二值化+锐化
            String processedPath = preprocessForOCR(imagePath, 1800);
            String rawText = doOcrMultiPsm(processedPath);
            log.info("OCR rawText for license (first 800 chars): {}", rawText.length() > 800 ? rawText.substring(0, 800) + "..." : rawText);

            Map<String, Object> fields;
            if (deepSeekService.isEnabled()) {
                fields = parseLicenseWithAI(rawText);
                if (fields == null || fields.isEmpty()) {
                    log.warn("DeepSeek parse returned empty, falling back to regex");
                    fields = parseLicenseFields(rawText);
                }
            } else {
                fields = parseLicenseFields(rawText);
            }

            new File(imagePath).delete();
            new File(processedPath).delete();
            fields.put("rawText", rawText);
            log.info("License OCR result fields: {}", fields.keySet());
            return Result.success(fields);
        } catch (Exception e) {
            log.error("License OCR failed", e);
            return Result.error("OCR识别失败: " + e.getMessage());
        }
    }

    @PostMapping("/idcard")
    public Result<Map<String, Object>> recognizeIdCard(@RequestParam("file") MultipartFile file) {
        try {
            String imagePath = saveTempFile(file);
            // 身份证预处理：缩放保证最小分辨率
            String processedPath = preprocessForOCR(imagePath, 1200);
            String rawText = doIdCardOcr(processedPath);
            log.info("OCR rawText for idcard (first 500 chars): {}", rawText.substring(0, Math.min(500, rawText.length())));

            Map<String, Object> fields;
            if (deepSeekService.isEnabled()) {
                fields = parseIdCardWithAI(rawText);
                if (fields == null || fields.isEmpty()) {
                    log.warn("DeepSeek parse returned empty for idcard, falling back to regex");
                    fields = parseIdCardFields(rawText);
                }
            } else {
                fields = parseIdCardFields(rawText);
            }

            new File(imagePath).delete();
            new File(processedPath).delete();
            fields.put("rawText", rawText);
            return Result.success(fields);
        } catch (Exception e) {
            log.error("IdCard OCR failed", e);
            return Result.error("OCR识别失败: " + e.getMessage());
        }
    }

    private String saveTempFile(MultipartFile file) throws IOException {
        Path uploadPath = Paths.get(uploadDir, "ocr");
        Files.createDirectories(uploadPath);
        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(filename);
        file.transferTo(filePath.toFile());
        return filePath.toString();
    }

    /**
     * OCR 图片预处理：缩放 + 灰度 + 自适应二值化 + 锐化
     * 解决 Tesseract "Image too small to scale" 及中文识别率低的问题
     */
    private String preprocessForOCR(String imagePath, int minWidth) {
        try {
            BufferedImage original = ImageIO.read(new File(imagePath));
            if (original == null) return imagePath;

            int w = original.getWidth();
            int h = original.getHeight();

            // 1. 如果图片太小，等比例放大到 minWidth
            if (w < minWidth) {
                double scale = (double) minWidth / w;
                int newW = (int) (w * scale);
                int newH = (int) (h * scale);
                BufferedImage scaled = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_RGB);
                Graphics2D g2d = scaled.createGraphics();
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g2d.drawImage(original, 0, 0, newW, newH, null);
                g2d.dispose();
                w = newW;
                h = newH;
                original = scaled;
            }

            // 2. 转为灰度图并增强对比度
            BufferedImage gray = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    int rgb = original.getRGB(x, y);
                    int r = (rgb >> 16) & 0xFF;
                    int g = (rgb >> 8) & 0xFF;
                    int b = rgb & 0xFF;
                    int grayVal = (int) (0.299 * r + 0.587 * g + 0.114 * b);
                    // 增强对比度：拉伸到全范围
                    grayVal = Math.min(255, Math.max(0, (grayVal - 30) * 255 / 195));
                    int grayRgb = (grayVal << 16) | (grayVal << 8) | grayVal;
                    gray.setRGB(x, y, grayRgb);
                }
            }

            // 3. 自适应二值化：根据局部区域动态阈值
            BufferedImage binary = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
            int blockSize = 15; // 局部区域大小
            int c = 8; // 常数偏移
            int[][] integral = new int[w][h]; // 积分图加速

            for (int y = 0; y < h; y++) {
                int sum = 0;
                for (int x = 0; x < w; x++) {
                    int grayVal = gray.getRGB(x, y) & 0xFF;
                    sum += grayVal;
                    integral[x][y] = (y > 0 ? integral[x][y-1] : 0) + sum;
                }
            }

            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    int x1 = Math.max(0, x - blockSize / 2);
                    int y1 = Math.max(0, y - blockSize / 2);
                    int x2 = Math.min(w - 1, x + blockSize / 2);
                    int y2 = Math.min(h - 1, y + blockSize / 2);

                    int count = (x2 - x1 + 1) * (y2 - y1 + 1);
                    int sum = integral[x2][y2]
                            - (x1 > 0 ? integral[x1-1][y2] : 0)
                            - (y1 > 0 ? integral[x2][y1-1] : 0)
                            + (x1 > 0 && y1 > 0 ? integral[x1-1][y1-1] : 0);

                    int threshold = sum / count - c;
                    int grayVal = gray.getRGB(x, y) & 0xFF;
                    int binVal = grayVal > threshold ? 255 : 0;
                    int binRgb = (binVal << 16) | (binVal << 8) | binVal;
                    binary.setRGB(x, y, binRgb);
                }
            }

            // 4. 锐化
            float[] sharpenKernel = {
                0, -1,  0,
               -1,  5, -1,
                0, -1,  0
            };
            BufferedImage sharpened = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
            ConvolveOp op = new ConvolveOp(new Kernel(3, 3, sharpenKernel), ConvolveOp.EDGE_NO_OP, null);
            op.filter(binary, sharpened);

            // 保存预处理后的图片
            String processedPath = imagePath + "_preprocessed.png";
            ImageIO.write(sharpened, "png", new File(processedPath));
            log.info("Preprocessed image: {}x{} -> {} (grayscale+contrast+binary+sharpen)", original.getWidth(), original.getHeight(), processedPath);
            return processedPath;
        } catch (Exception e) {
            log.warn("Image preprocessing failed, using original: {}", e.getMessage());
            return imagePath;
        }
    }

    /**
     * Tesseract OCR 识别
     * 使用 PSM 3 (Fully automatic page segmentation) 适合混合排版的营业执照
     * 支持中文简体+英文
     */
    private String doOcr(String imagePath) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(
                "tesseract", imagePath, "stdout",
                "-l", "chi_sim+eng",
                "--psm", "3",
                "--dpi", "300"
        );
        pb.redirectErrorStream(true);
        Process process = pb.start();

        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }
        int exitCode = process.waitFor();
        log.info("Tesseract exit code: {}, output length: {}", exitCode, output.length());

        String result = output.toString().trim();
        if (result.isEmpty()) {
            log.warn("Tesseract produced empty output for image: {}", imagePath);
        }
        // 清理常见 OCR 噪声
        result = result.replaceAll("\\r", "").replaceAll("[ \\t]+", " ");
        return result;
    }

    /**
     * 使用 DeepSeek 大模型解析营业执照 OCR 原始文本
     * LLM 对脏文本的容错能力远超正则表达式
     */
    private Map<String, Object> parseLicenseWithAI(String rawText) {
        String systemPrompt = "你是一个专业的OCR后处理助手，负责从杂乱的OCR识别文本中提取营业执照的关键字段。\n" +
                "要求：\n" +
                "1. 仔细分析提供的OCR文本，识别其中的营业执照信息\n" +
                "2. 严格按照以下JSON格式返回结果，不要包含任何额外文字或markdown标记\n" +
                "3. 如果某字段无法识别，填空字符串\n" +
                "4. 注意：中文营业执照中'名称'指的是企业名称/公司名称，'法定代表人'是法人姓名\n" +
                "5. 信用代码通常是18位数字加字母组合\n" +
                "6. 注册资本可能有多种格式（如'人民币100万元'、'美元150.000万'等）\n" +
                "\n" +
                "返回格式：\n" +
                "{\n" +
                "  \"companyName\": \"企业完整名称\",\n" +
                "  \"creditCode\": \"统一社会信用代码（18位）\",\n" +
                "  \"legalPerson\": \"法定代表人姓名\",\n" +
                "  \"registeredCapital\": \"注册资本完整表述\",\n" +
                "  \"establishDate\": \"成立日期\",\n" +
                "  \"address\": \"住所/地址\",\n" +
                "  \"businessScope\": \"经营范围\",\n" +
                "  \"businessType\": \"企业类型\"\n" +
                "}";

        String userPrompt = "以下是通过OCR识别得到的营业执照原始文本（可能存在识别错误、乱序、多余字符等问题），请提取关键字段：\n\n" +
                "---OCR原始文本开始---\n" +
                rawText + "\n" +
                "---OCR原始文本结束---\n\n" +
                "请仔细分析，尽可能准确地提取各字段信息。";

        try {
            String response = deepSeekService.chat(systemPrompt, userPrompt);
            if (response == null) return null;

            // 清理可能的 markdown 标记
            response = response.replaceAll("```json\\s*|```", "").trim();

            @SuppressWarnings("unchecked")
            Map<String, Object> result = objectMapper.readValue(response, Map.class);
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 多 PSM 模式 OCR（营业执照等复杂文档）
     * 尝试 PSM 3/4/6，取最佳结果
     */
    private String doOcrMultiPsm(String imagePath) throws Exception {
        int[] psmModes = {3, 4, 6};
        String bestResult = "";

        for (int psm : psmModes) {
            try {
                ProcessBuilder pb = new ProcessBuilder(
                        "tesseract", imagePath, "stdout",
                        "-l", "chi_sim+eng",
                        "--psm", String.valueOf(psm),
                        "--dpi", "300"
                );
                pb.redirectErrorStream(true);
                Process process = pb.start();

                StringBuilder output = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.startsWith("Estimating resolution") || line.startsWith("Warning")
                            || line.startsWith("Detected") || line.startsWith("Image too small"))
                            continue;
                        output.append(line).append("\n");
                    }
                }
                process.waitFor();

                String result = output.toString().trim();
                result = result.replaceAll("\\r", "").replaceAll("[ \\t]+", " ");

                if (result.length() > bestResult.length()) {
                    bestResult = result;
                }
            } catch (Exception e) {
                log.warn("Tesseract PSM {} failed for license: {}", psm, e.getMessage());
            }
        }

        log.info("License OCR best result ({} chars): {}", bestResult.length(), bestResult.length() > 300 ? bestResult.substring(0, 300) + "..." : bestResult);
        return bestResult;
    }

    /**
     * 身份证专用 OCR 识别
     * 身份证有防伪底纹、头像等干扰，普通 PSM 模式很难识别
     * 策略：尝试 PSM 11（稀疏文本）、PSM 7（单行文本）、PSM 3（自动）
     * 取最佳结果
     */
    private String doIdCardOcr(String imagePath) throws Exception {
        // 多种 PSM 模式，取结果最长的
        int[] psmModes = {11, 7, 3};
        String bestResult = "";

        for (int psm : psmModes) {
            try {
                ProcessBuilder pb = new ProcessBuilder(
                        "tesseract", imagePath, "stdout",
                        "-l", "chi_sim+eng",
                        "--psm", String.valueOf(psm),
                        "--oem", "1",
                        "--dpi", "300"
                );
                pb.redirectErrorStream(true);
                Process process = pb.start();

                StringBuilder output = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        // 过滤 Tesseract 自身的诊断信息
                        if (line.startsWith("Estimating resolution") || line.startsWith("Warning")) continue;
                        output.append(line).append("\n");
                    }
                }
                process.waitFor();

                String result = output.toString().trim();
                // 过滤纯数字和噪音行
                result = result.replaceAll("(?m)^\\s*\\d+\\s*$", "").trim();
                result = result.replaceAll("\\r", "").replaceAll("[ \\t]+", " ");

                if (result.length() > bestResult.length()) {
                    bestResult = result;
                }
            } catch (Exception e) {
                log.warn("Tesseract PSM {} failed: {}", psm, e.getMessage());
            }
        }

        log.info("ID card OCR best result length: {} chars", bestResult.length());
        return bestResult;
    }

    /**
     * 使用 DeepSeek 大模型解析身份证 OCR 原始文本（只提取姓名和身份证号）
     */
    private Map<String, Object> parseIdCardWithAI(String rawText) {
        String systemPrompt = "你是一个专业的OCR后处理助手，负责从杂乱的OCR识别文本中提取身份证的关键字段。\n" +
                "重要提示：身份证OCR文本通常包含大量噪声和识别错误，请尽力从混乱文本中推断正确信息。\n" +
                "要求：\n" +
                "1. 只提取姓名和身份证号码两个字段\n" +
                "2. 严格按照以下JSON格式返回结果，不要包含任何额外文字或markdown标记\n" +
                "3. 如果某字段无法识别，填空字符串\n" +
                "4. 身份证号码为18位数字加字母组合（末尾可能是X）\n" +
                "5. 姓名通常为2-4个中文字符，位于身份证照片下方或附近\n" +
                "6. OCR可能将中文姓名识别为形近字，请根据上下文纠正\n" +
                "\n" +
                "返回格式：\n" +
                "{\n" +
                "  \"name\": \"识别的姓名\",\n" +
                "  \"idNumber\": \"识别的18位身份证号\"\n" +
                "}";

        String userPrompt = "以下是通过OCR识别得到的身份证原始文本（可能存在识别错误、乱序、多余字符等问题），请尽力提取姓名和身份证号：\n\n" +
                "---OCR原始文本开始---\n" +
                rawText + "\n" +
                "---OCR原始文本结束---\n\n" +
                "注意：即使OCR文本非常混乱，也请尽力推断。如果在原始文本中找不到明确信息，请返回空字符串。";

        try {
            String response = deepSeekService.chat(systemPrompt, userPrompt);
            if (response == null) return null;

            response = response.replaceAll("```json\\s*|```", "").trim();

            @SuppressWarnings("unchecked")
            Map<String, Object> result = objectMapper.readValue(response, Map.class);
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 正则匹配兜底方案（当 DeepSeek 未启用时使用）
     * 针对营业执照真实文字布局优化的正则
     */
    private Map<String, Object> parseLicenseFields(String text) {
        Map<String, Object> fields = new LinkedHashMap<>();

        // 营业执照的文字布局通常是表格形式
        // "名称" 后面跟着企业名称
        fields.put("companyName", extractByLabel(text, "名\\s*称"));
        // 统一社会信用代码：18位
        fields.put("creditCode", extractPattern(text, "[0-9A-HJ-NPQRTUWXY]{2}[0-9]{6}[0-9A-HJ-NPQRTUWXY]{10}"));
        // 法定代表人
        fields.put("legalPerson", extractByLabel(text, "法定代表人|法\\s*定\\s*代\\s*表\\s*人"));
        // 注册资本
        fields.put("registeredCapital", extractByLabel(text, "注册资本|注\\s*册\\s*资\\s*本"));
        // 成立日期
        fields.put("establishDate", extractByLabel(text, "成立日期|成\\s*立\\s*日\\s*期"));
        // 住所/地址
        fields.put("address", extractByLabel(text, "住\\s*所|住\\s*址|地址"));
        // 经营范围
        fields.put("businessScope", extractByLabel(text, "经\\s*营\\s*范\\s*围|经\\s*营\\s*范\\s*围"));
        // 企业类型
        fields.put("businessType", extractByLabel(text, "类\\s*型|企业类型"));

        return fields;
    }

    /**
     * 身份证正则匹配兜底方案
     */
    private Map<String, Object> parseIdCardFields(String text) {
        Map<String, Object> fields = new LinkedHashMap<>();
        // 姓名：通常2-4个汉字
        fields.put("name", extractName(text));
        // 身份证号：18位，末尾可能是X
        fields.put("idNumber", extractPattern(text, "[0-9]{17}[0-9Xx]"));
        return fields;
    }

    /**
     * 从 OCR 文本中提取姓名（2-4个中文字符）
     * 身份证上姓名位置通常在 OCR 文本开头附近
     */
    private String extractName(String text) {
        // 策略1：匹配"姓名"标签后的2-4个中文字符
        java.util.regex.Pattern p = java.util.regex.Pattern.compile("姓\\s*名\\s*[:：]?\\s*([\\u4e00-\\u9fa5]{2,4})");
        java.util.regex.Matcher m = p.matcher(text);
        if (m.find()) return m.group(1);

        // 策略2：在文本开头找2-4个连续中文字符（身份证姓名通常在开头）
        p = java.util.regex.Pattern.compile("^[^\\u4e00-\\u9fa5]*([\\u4e00-\\u9fa5]{2,4})");
        m = p.matcher(text.trim());
        if (m.find()) {
            String name = m.group(1);
            // 排除"姓名"、"性别"等标签词
            if (!name.equals("姓名") && !name.equals("性别") && !name.equals("民族") &&
                !name.equals("出生") && !name.equals("住址") && !name.equals("号码")) {
                return name;
            }
        }

        // 策略3：匹配标签"名称"后面的2-4个中文
        p = java.util.regex.Pattern.compile("名\\s*称\\s*[:：]?\\s*([\\u4e00-\\u9fa5]{2,4})");
        m = p.matcher(text);
        if (m.find()) return m.group(1);

        return "";
    }

    /**
     * 根据标签提取值（如 "名 称" 后面的内容）
     */
    private String extractByLabel(String text, String labelRegex) {
        // 先去掉多余换行，保持阅读顺序
        String normalized = text.replaceAll("\\n+", " ").replaceAll("\\s{2,}", " ");
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(
                labelRegex + "\\s*[:：]?\\s*([^\\s]{2,30}?)(?=\\s{2,}|$|\\t|\\|)"
        );
        java.util.regex.Matcher m = p.matcher(normalized);
        if (m.find()) {
            return m.group(1).trim();
        }
        // 宽松匹配
        p = java.util.regex.Pattern.compile(labelRegex + "\\s*[:：]?\\s*(.{2,30}?)\\s*$", java.util.regex.Pattern.MULTILINE);
        m = p.matcher(normalized);
        if (m.find()) {
            return m.group(1).trim();
        }
        return "";
    }

    /**
     * 根据正则模式提取值
     */
    private String extractPattern(String text, String pattern) {
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(text);
        if (m.find()) {
            return m.group();
        }
        return "";
    }
}
