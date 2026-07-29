import os, asyncio
from playwright.async_api import async_playwright

OUT = "/Users/wangwei/WorkBuddy/2026-07-23-09-20-10/mall-ecosystem/video_build/explore"
os.makedirs(OUT, exist_ok=True)

async def main():
    async with async_playwright() as p:
        browser = await p.chromium.launch(channel="chrome", headless=True, args=["--no-sandbox", "--disable-gpu"])
        page = await browser.new_page(viewport={"width": 1920, "height": 1080}, device_scale_factor=1)
        try:
            await page.goto("http://localhost:5173/login", wait_until="networkidle", timeout=30000)
            await page.wait_for_timeout(1000)
            # Try multiple possible selectors
            for sel in ['[placeholder*="用户名"]', 'input[type="text"]', 'input#username', 'input.el-input__inner']:
                try:
                    await page.locator(sel).first.fill("admin")
                    break
                except Exception:
                    pass
            for sel in ['[placeholder*="密码"]', 'input[type="password"]', 'input#password']:
                try:
                    await page.locator(sel).first.fill("admin123")
                    break
                except Exception:
                    pass
            # Click login button with text 登录
            btn = page.locator('button:has-text("登")').first
            await btn.click()
            await page.wait_for_timeout(4000)
            await page.screenshot(path=os.path.join(OUT, "5173_after_login.png"), full_page=False)
            print(f"After login URL: {page.url}")
            # Try to find menu items text
            menus = await page.locator('.el-menu-item, .el-sub-menu__title, .menu-item, .ant-menu-item').all_inner_texts()
            print("Menus:", [m.strip() for m in menus if m.strip()][:30])
        except Exception as e:
            print("ERR", e)
            await page.screenshot(path=os.path.join(OUT, "5173_login_err.png"), full_page=False)
        await browser.close()

asyncio.run(main())
