<template>
  <div class="page-container">
    <div class="page-header">
      <h2>📦 商品管理</h2>
      <div style="display:flex;gap:12px">
        <el-button type="primary" @click="openAddProduct">+ 商品引入</el-button>
        <el-button type="success" @click="openBenefitDialog">+ 权益引入</el-button>
      </div>
    </div>

    <div class="table-container">
    <el-table :data="pagedProducts" border stripe>
      <el-table-column prop="productCode" label="商品编号" width="160" />
      <el-table-column label="商品图片" width="100">
        <template #default="{ row }">
          <img v-if="row.imageUrls || row.productImage" :src="getFirstImage(row)" style="width: 60px; height: 60px; object-fit: cover; border-radius: 4px" />
          <span v-else class="no-image">无图</span>
        </template>
      </el-table-column>
      <el-table-column prop="productName" label="商品名称" width="200" />
      <el-table-column prop="productType" label="商品类型" width="100">
        <template #default="{ row }">
          <el-tag :type="getProductTypeTag(row.productType)">{{ getProductTypeText(row.productType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="categoryName" label="商品分类" width="120">
        <template #default="{ row }">
          <el-tag>{{ row.categoryName || row.category }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="brand" label="品牌" width="100" />
      <el-table-column prop="price" label="售价" width="100">
        <template #default="{ row }">¥{{ (row.price || 0).toFixed(2) }}</template>
      </el-table-column>
      <el-table-column prop="marketPrice" label="市场价" width="100">
        <template #default="{ row }"><span style="text-decoration:line-through;color:#999">¥{{ (row.marketPrice || 0).toFixed(2) }}</span></template>
      </el-table-column>
      <el-table-column prop="stock" label="库存" width="80" />
      <el-table-column prop="salesCount" label="销量" width="80" />
      <el-table-column label="审核状态" width="140">
        <template #default="{ row }">
          <el-tag :type="getAuditStatusType(row)">{{ getAuditStatusText(row) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" fixed="right" width="400">
        <template #default="{ row }">
          <el-button size="small" @click="editProduct(row)">编辑</el-button>
          <el-button size="small" @click="viewProduct(row)">查看</el-button>
          <el-button size="small" type="success" @click="extractSellingPoints(row)">✨ 卖点提炼</el-button>
          <el-button v-if="row.status === 'REJECTED'" size="small" type="warning" @click="resubmitProduct(row)">重新提交</el-button>
          <el-button v-if="row.status === 'ON_SHELF'" size="small" type="info" @click="toggleShelf(row)">下架</el-button>
          <el-button v-else-if="row.status === 'OFF_SHELF'" size="small" type="success" @click="toggleShelf(row)">上架</el-button>
          <el-button v-else size="small" type="info" disabled>上架</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div style="display:flex;justify-content:center;margin-top:16px">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="products.length"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
      />
    </div>
    </div>

    <el-dialog v-model="showModal" :title="editingProduct ? '编辑商品' : '商品引入'" width="900px" :close-on-click-modal="false">
      <el-form :model="form" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="所属商户" required>
              <el-select v-model="form.merchantId" placeholder="请选择商户">
                <el-option v-for="m in merchants" :key="m.id" :value="m.id" :label="m.merchantName" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="商品编号">
              <el-input v-model="form.productCode" :disabled="!!editingProduct" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="商品名称" required>
          <el-input v-model="form.productName" placeholder="请输入商品名称" />
        </el-form-item>

        <el-row :gutter="20" v-if="form.productType !== 'BENEFIT'">
          <el-col :span="12">
            <el-form-item label="商品分类" required>
              <el-select v-model="form.categoryId" placeholder="请选择分类">
                <el-option v-for="c in categories" :key="c.id" :value="c.id" :label="c.categoryName" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="品牌">
              <el-input v-model="form.brand" placeholder="请输入品牌名称" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">商品信息</el-divider>
        <el-form-item label="商品图片">
          <el-upload
            action="/api/product/upload"
            list-type="picture-card"
            :file-list="imageList"
            :on-success="handleImageUpload"
            :on-remove="handleImageRemove"
          >
            <div>
              <el-icon><Plus /></el-icon>
              <div style="margin-top: 6px">上传图片</div>
            </div>
          </el-upload>
        </el-form-item>

        <el-form-item label="商品介绍" v-if="form.productType !== 'BENEFIT'">
          <div style="display:flex;align-items:flex-start;gap:8px">
            <el-input v-model="form.description" type="textarea" :rows="4" placeholder="请输入商品介绍" style="flex:1" />
            <el-button size="small" type="primary" plain style="margin-top:2px;flex-shrink:0" :loading="proofreading" @click="doProofread('description')">🤖 AI校对</el-button>
          </div>
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="售价" required>
              <div style="display:flex;align-items:center;gap:6px">
                <el-input-number v-model="form.price" :min="0" :precision="2" placeholder="请输入售价" style="width:130px" />
                <el-button size="small" type="warning" plain :loading="priceResearching" @click="doPriceResearch">💹 价格摸排</el-button>
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="8" v-if="form.productType !== 'BENEFIT'">
            <el-form-item label="市场价">
              <el-input-number v-model="form.marketPrice" :min="0" :precision="2" placeholder="请输入市场价" />
            </el-form-item>
          </el-col>
          <el-col :span="8" v-if="form.productType !== 'BENEFIT'">
            <el-form-item label="会员价">
              <el-input-number v-model="form.vipPrice" :min="0" :precision="2" placeholder="请输入会员价" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="8" v-if="form.productType !== 'BENEFIT'">
            <el-form-item label="库存" required>
              <el-input-number v-model="form.stock" :min="0" placeholder="请输入库存" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="商品类型">
              <template v-if="form.productType === 'BENEFIT'">
                <el-tag type="success" style="height:32px;line-height:32px">权益商品</el-tag>
              </template>
              <el-select v-else v-model="form.productType">
                <el-option label="实物商品" value="PHYSICAL" />
                <el-option label="虚拟商品" value="VIRTUAL" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="商品详情" v-if="form.productType !== 'BENEFIT'">
          <div style="display:flex;align-items:flex-start;gap:8px">
            <RichTextEditor v-model="form.detail" style="flex:1" />
            <el-button size="small" type="primary" plain style="margin-top:2px;flex-shrink:0" :loading="proofreading" @click="doProofread('detail')">🤖 AI校对</el-button>
          </div>
        </el-form-item>

        <el-form-item label="卖点标签" v-if="form.productType !== 'BENEFIT'">
          <el-input v-model="form.tags" placeholder="多个标签用逗号分隔" />
        </el-form-item>

        <el-divider content-position="left">🤖 AI辅助工具</el-divider>
        <el-form-item>
          <div style="display:flex;gap:10px;flex-wrap:wrap;align-items:center">
            <el-button size="small" type="success" plain :loading="autofilling" @click="doAutoFill">🤖 AI辅助补全</el-button>
            <span style="font-size:12px;color:#999">AI将根据商品名称和已有信息，智能补全描述、详情、标签、售价等字段</span>
          </div>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button type="primary" @click="submitProduct">保存</el-button>
      </template>
    </el-dialog>

    <!-- 查看详情弹窗 -->
    <el-dialog v-model="showViewDialog" title="商品详情" width="800px" :close-on-click-modal="false">
      <template v-if="viewProductData">
        <el-tabs v-model="detailTab">
          <el-tab-pane label="基本信息" name="basic">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="商品编号">{{ viewProductData.productCode }}</el-descriptions-item>
              <el-descriptions-item label="商品名称">{{ viewProductData.productName }}</el-descriptions-item>
              <el-descriptions-item label="商品类型">
                <el-tag :type="getProductTypeTag(viewProductData.productType)">{{ getProductTypeText(viewProductData.productType) }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="所属商户">{{ viewProductData.merchantName || '-' }}</el-descriptions-item>
              <el-descriptions-item v-if="viewProductData.productType !== 'BENEFIT'" label="商品分类">{{ viewProductData.categoryName || viewProductData.category || '-' }}</el-descriptions-item>
              <el-descriptions-item v-if="viewProductData.productType !== 'BENEFIT'" label="品牌">{{ viewProductData.brand || '-' }}</el-descriptions-item>
              <el-descriptions-item label="售价">¥{{ (viewProductData.price || 0).toFixed(2) }}</el-descriptions-item>
              <el-descriptions-item v-if="viewProductData.productType !== 'BENEFIT'" label="市场价">¥{{ (viewProductData.marketPrice || 0).toFixed(2) }}</el-descriptions-item>
              <el-descriptions-item v-if="viewProductData.productType !== 'BENEFIT'" label="会员价">¥{{ (viewProductData.vipPrice || 0).toFixed(2) }}</el-descriptions-item>
              <el-descriptions-item v-if="viewProductData.productType !== 'BENEFIT'" label="库存">{{ viewProductData.stock || 0 }}</el-descriptions-item>
              <el-descriptions-item v-if="viewProductData.productType !== 'BENEFIT'" label="销量">{{ viewProductData.salesCount || 0 }}</el-descriptions-item>
              <el-descriptions-item label="审核状态">
                <el-tag :type="getAuditStatusType(viewProductData)">{{ getAuditStatusText(viewProductData) }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="当前状态">
                <el-tag :type="getStatusType(viewProductData.status)">{{ getStatusText(viewProductData.status) }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="创建时间">{{ viewProductData.createTime || '-' }}</el-descriptions-item>
              <el-descriptions-item v-if="viewProductData.productType !== 'BENEFIT'" label="卖点标签">{{ viewProductData.tags || '-' }}</el-descriptions-item>
              <el-descriptions-item v-if="viewProductData.productType !== 'BENEFIT'" label="商品介绍" :span="2">{{ viewProductData.description || '-' }}</el-descriptions-item>
              <el-descriptions-item v-if="viewProductData.productType !== 'BENEFIT' && viewProductData.detail" label="商品详情" :span="2">
                <div v-html="viewProductData.detail" style="max-height:200px;overflow-y:auto"></div>
              </el-descriptions-item>

              <template v-if="viewProductData.productType === 'BENEFIT' && viewProductData._benefit">
                <el-descriptions-item label="权益类型">{{ getBenefitTypeText(viewProductData._benefit.benefitType) }}</el-descriptions-item>
                <el-descriptions-item label="兑换方式">{{ getExchangeMethodText(viewProductData._benefit.exchangeMethod) }}</el-descriptions-item>
                <el-descriptions-item label="面值">¥{{ (viewProductData._benefit.faceValue || 0).toFixed(2) }}</el-descriptions-item>
                <el-descriptions-item label="结算价">¥{{ (viewProductData._benefit.settlePrice || 0).toFixed(2) }}</el-descriptions-item>
                <el-descriptions-item label="有效期类型">{{ getValidityTypeText(viewProductData._benefit.validityType) }}</el-descriptions-item>
                <el-descriptions-item label="有效天数">{{ viewProductData._benefit.validityType === 'DAYS_AFTER_RECEIVE' ? viewProductData._benefit.validityDays + '天' : '-' }}</el-descriptions-item>
                <el-descriptions-item v-if="viewProductData._benefit.validityType === 'FIXED_DATE'" label="有效期范围">{{ viewProductData._benefit.validityStart || '-' }} ~ {{ viewProductData._benefit.validityEnd || '-' }}</el-descriptions-item>
                <el-descriptions-item label="总库存">{{ viewProductData._benefit.stockTotal || '-' }}</el-descriptions-item>
                <el-descriptions-item label="每日限兑">{{ viewProductData._benefit.stockDailyLimit || '-' }}</el-descriptions-item>
                <el-descriptions-item label="每人限兑">{{ viewProductData._benefit.stockPerUser || '-' }}</el-descriptions-item>
                <el-descriptions-item label="联系方式">{{ viewProductData._benefit.supplierContact || '-' }}</el-descriptions-item>
                <el-descriptions-item label="退款政策">{{ getRefundPolicyText(viewProductData._benefit.refundPolicy) }}</el-descriptions-item>
                <el-descriptions-item label="使用规则" :span="2">{{ viewProductData._benefit.usageRules || '-' }}</el-descriptions-item>
                <el-descriptions-item label="适用范围" :span="2">{{ viewProductData._benefit.applicableScope || '-' }}</el-descriptions-item>
                <el-descriptions-item label="详细说明" :span="2">{{ viewProductData._benefit.detailDesc || '-' }}</el-descriptions-item>
                <el-descriptions-item label="权益描述" :span="2">{{ viewProductData._benefit.benefitDescription || '-' }}</el-descriptions-item>
              </template>
            </el-descriptions>
          </el-tab-pane>
          <el-tab-pane label="审核记录" name="audit">
            <el-timeline v-if="productAuditLogs.length > 0">
              <el-timeline-item
                v-for="log in productAuditLogs"
                :key="log.id"
                :timestamp="log.time"
                placement="top"
                :color="log.result === '驳回' ? '#f56c6c' : '#409eff'"
              >
                <el-card shadow="hover">
                  <p><strong>{{ log.nodeName }}</strong></p>
                  <p>操作人：{{ log.operator || '-' }}</p>
                  <p v-if="log.result">
                    审核结果：<el-tag :type="log.result === '通过' ? 'success' : 'danger'" size="small">{{ log.result }}</el-tag>
                  </p>
                  <p v-if="log.comment" style="color: #f56c6c;">审核意见：{{ log.comment }}</p>
                </el-card>
              </el-timeline-item>
            </el-timeline>
            <el-empty v-else description="暂无审核记录" />
          </el-tab-pane>
        </el-tabs>
      </template>
    </el-dialog>

    <!-- 编辑商品弹窗（含驳回提示） -->
    <el-dialog v-model="showEditWithRejectDialog" :title="resubmitTarget ? '重新提交商品' : '编辑商品'" width="900px" :close-on-click-modal="false">
      <el-alert v-if="resubmitTarget?.rejectReason" type="error" title="驳回原因" :closable="false" style="margin-bottom: 16px">
        <template #default>
          <p style="margin: 0;">{{ resubmitTarget.rejectReason }}</p>
          <p v-if="resubmitTarget.auditTime" style="margin: 4px 0 0; font-size: 12px; color: #909399;">
            审核时间：{{ resubmitTarget.auditTime }} | 审核人：{{ resubmitTarget.auditor || '系统' }}
          </p>
        </template>
      </el-alert>
      <el-form :model="form" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="所属商户" required>
              <el-select v-model="form.merchantId" placeholder="请选择商户">
                <el-option v-for="m in merchants" :key="m.id" :value="m.id" :label="m.merchantName" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="商品编号">
              <el-input v-model="form.productCode" disabled />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="商品名称" required>
          <el-input v-model="form.productName" placeholder="请输入商品名称" />
        </el-form-item>

        <el-row :gutter="20" v-if="form.productType !== 'BENEFIT'">
          <el-col :span="12">
            <el-form-item label="商品分类" required>
              <el-select v-model="form.categoryId" placeholder="请选择分类">
                <el-option v-for="c in categories" :key="c.id" :value="c.id" :label="c.categoryName" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="品牌">
              <el-input v-model="form.brand" placeholder="请输入品牌名称" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">商品信息</el-divider>
        <el-form-item label="商品图片">
          <el-upload
            action="/api/product/upload"
            list-type="picture-card"
            :file-list="imageList"
            :on-success="handleImageUpload"
          >
            <div>
              <el-icon><Plus /></el-icon>
              <div style="margin-top: 6px">上传图片</div>
            </div>
          </el-upload>
        </el-form-item>

        <el-form-item label="商品介绍" v-if="form.productType !== 'BENEFIT'">
          <div style="display:flex;align-items:flex-start;gap:8px">
            <el-input v-model="form.description" type="textarea" :rows="4" placeholder="请输入商品介绍" style="flex:1" />
            <el-button size="small" type="primary" plain style="margin-top:2px;flex-shrink:0" :loading="proofreading" @click="doProofread('description')">🤖 AI校对</el-button>
          </div>
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="售价" required>
              <div style="display:flex;align-items:center;gap:6px">
                <el-input-number v-model="form.price" :min="0" :precision="2" placeholder="请输入售价" style="width:130px" />
                <el-button size="small" type="warning" plain :loading="priceResearching" @click="doPriceResearch">💹 价格摸排</el-button>
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="8" v-if="form.productType !== 'BENEFIT'">
            <el-form-item label="市场价">
              <el-input-number v-model="form.marketPrice" :min="0" :precision="2" placeholder="请输入市场价" />
            </el-form-item>
          </el-col>
          <el-col :span="8" v-if="form.productType !== 'BENEFIT'">
            <el-form-item label="会员价">
              <el-input-number v-model="form.vipPrice" :min="0" :precision="2" placeholder="请输入会员价" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="8" v-if="form.productType !== 'BENEFIT'">
            <el-form-item label="库存" required>
              <el-input-number v-model="form.stock" :min="0" placeholder="请输入库存" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="商品类型">
              <template v-if="form.productType === 'BENEFIT'">
                <el-tag type="success" style="height:32px;line-height:32px">权益商品</el-tag>
              </template>
              <el-select v-else v-model="form.productType">
                <el-option label="实物商品" value="PHYSICAL" />
                <el-option label="虚拟商品" value="VIRTUAL" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="商品详情" v-if="form.productType !== 'BENEFIT'">
          <div style="display:flex;align-items:flex-start;gap:8px">
            <RichTextEditor v-model="form.detail" style="flex:1" />
            <el-button size="small" type="primary" plain style="margin-top:2px;flex-shrink:0" :loading="proofreading" @click="doProofread('detail')">🤖 AI校对</el-button>
          </div>
        </el-form-item>

        <el-form-item label="卖点标签" v-if="form.productType !== 'BENEFIT'">
          <el-input v-model="form.tags" placeholder="多个标签用逗号分隔" />
        </el-form-item>

        <el-divider content-position="left">🤖 AI辅助工具</el-divider>
        <el-form-item>
          <div style="display:flex;gap:10px;flex-wrap:wrap;align-items:center">
            <el-button size="small" type="success" plain :loading="autofilling" @click="doAutoFill">🤖 AI辅助补全</el-button>
            <span style="font-size:12px;color:#999">AI将根据商品名称和已有信息，智能补全描述、详情、标签、售价等字段</span>
          </div>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button type="primary" @click="submitResubmit">重新提交</el-button>
      </template>
    </el-dialog>

    <!-- 权益引入弹窗 -->
    <el-dialog v-model="showBenefitModal" title="权益引入" width="700px" :close-on-click-modal="false" destroy-on-close>
      <el-form :model="benefitForm" label-width="100px">
        <el-form-item label="权益名称" required>
          <el-input v-model="benefitForm.benefitName" placeholder="如：腾讯视频VIP月卡" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="所属商户">
              <el-select v-model="benefitForm.supplierId" placeholder="选择已入驻商户" style="width:100%" @change="onSupplierChange" clearable filterable>
                <el-option v-for="m in merchants" :key="m.id" :value="m.id" :label="m.merchantName" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系方式">
              <el-input v-model="benefitForm.supplierContact" placeholder="选择商户后自动填充" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="权益类型" required>
              <el-select v-model="benefitForm.benefitType" style="width:100%">
                <el-option label="会员权益" value="MEMBERSHIP" />
                <el-option label="优惠券" value="COUPON" />
                <el-option label="游戏点卡" value="GAME_POINTS" />
                <el-option label="数字内容" value="DIGITAL_CONTENT" />
                <el-option label="在线服务" value="SERVICE" />
                <el-option label="保险/延保" value="INSURANCE" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="兑换方式">
              <el-select v-model="benefitForm.exchangeMethod" style="width:100%">
                <el-option label="自动绑定" value="AUTO_BIND" />
                <el-option label="兑换码" value="CODE" />
                <el-option label="二维码核销" value="QR_CODE" />
                <el-option label="人工发放" value="MANUAL" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="权益描述">
          <el-input v-model="benefitForm.benefitDescription" type="textarea" :rows="3" placeholder="描述权益的核心内容和价值" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="8"><el-form-item label="售价"><el-input-number v-model="benefitForm.price" :min="0" :precision="2" controls-position="right" style="width:100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="面值"><el-input-number v-model="benefitForm.faceValue" :min="0" :precision="2" controls-position="right" style="width:100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="结算价"><el-input-number v-model="benefitForm.settlePrice" :min="0" :precision="2" controls-position="right" style="width:100%" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="有效期类型">
          <el-select v-model="benefitForm.validityType" style="width:100%">
            <el-option label="固定日期" value="FIXED_DATE" />
            <el-option label="领取后N天有效" value="DAYS_AFTER_RECEIVE" />
            <el-option label="长期有效" value="DURATION" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="benefitForm.validityType === 'DAYS_AFTER_RECEIVE'" label="有效天数">
          <el-input-number v-model="benefitForm.validityDays" :min="1" controls-position="right" style="width:100%" />
        </el-form-item>
        <el-row v-if="benefitForm.validityType === 'FIXED_DATE'" :gutter="16">
          <el-col :span="12"><el-form-item label="有效期起"><el-date-picker v-model="benefitForm.validityStart" type="date" placeholder="选择日期" style="width:100%" value-format="YYYY-MM-DD" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="有效期止"><el-date-picker v-model="benefitForm.validityEnd" type="date" placeholder="选择日期" style="width:100%" value-format="YYYY-MM-DD" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8"><el-form-item label="总库存"><el-input-number v-model="benefitForm.stockTotal" :min="0" controls-position="right" style="width:100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="每日限兑"><el-input-number v-model="benefitForm.stockDailyLimit" :min="0" controls-position="right" style="width:100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="每人限兑"><el-input-number v-model="benefitForm.stockPerUser" :min="0" controls-position="right" style="width:100%" /></el-form-item></el-col>
        </el-row>
        <el-divider content-position="left">商品信息</el-divider>
        <el-form-item label="商品图片">
          <el-upload
            action="/api/product/upload"
            list-type="picture-card"
            :file-list="benefitImageList"
            :on-success="handleBenefitImageUpload"
            :on-remove="handleBenefitImageRemove"
          >
            <div>
              <el-icon><Plus /></el-icon>
              <div style="margin-top: 6px">上传图片</div>
            </div>
          </el-upload>
        </el-form-item>
        <el-form-item label="使用规则">
          <el-input v-model="benefitForm.usageRules" type="textarea" :rows="2" placeholder="如：不可与其他优惠叠加" />
        </el-form-item>
        <el-form-item label="适用范围">
          <el-input v-model="benefitForm.applicableScope" placeholder="如：全平台通用" />
        </el-form-item>
        <el-form-item label="详细说明">
          <el-input v-model="benefitForm.detailDesc" type="textarea" :rows="3" placeholder="权益详细描述说明" />
        </el-form-item>
        <el-form-item label="退款政策">
          <el-select v-model="benefitForm.refundPolicy" style="width:100%">
            <el-option label="不可退款" value="NO_REFUND" />
            <el-option label="有条件退款" value="CONDITIONAL" />
            <el-option label="支持退款" value="FULL_REFUND" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" :loading="benefitSubmitting" @click="submitBenefit">提交权益引入</el-button>
      </template>
    </el-dialog>

    <!-- AI校对结果弹窗 -->
    <el-dialog v-model="showProofreadDialog" title="🤖 AI校对结果" width="800px" :close-on-click-modal="false">
      <div v-loading="proofreadLoading">
        <template v-if="proofreadResult">
          <div v-if="proofreadResult.issues && proofreadResult.issues.length > 0" style="margin-bottom:16px">
            <el-alert v-for="(issue, idx) in proofreadResult.issues" :key="idx" :title="issue.type" :description="`原文: ${issue.original} → 建议: ${issue.suggestion}（${issue.position}）`" type="warning" show-icon :closable="false" style="margin-bottom:8px" />
          </div>
          <div v-if="proofreadResult.summary" style="margin-bottom:16px">
            <h4>📝 整体评价</h4>
            <p>{{ proofreadResult.summary }}</p>
          </div>
          <div v-if="proofreadResult.optimizedContent" style="background:#f5f7fa;padding:16px;border-radius:8px;margin-bottom:16px">
            <h4>优化后内容</h4>
            <div v-html="proofreadResult.optimizedContent" style="max-height:300px;overflow-y:auto"></div>
          </div>
        </template>
        <el-empty v-else-if="!proofreadLoading" description="暂无校对结果" />
      </div>
      <template #footer>
        <el-button type="primary" @click="fillProofreadResult" :disabled="!proofreadResult?.optimizedContent">一键回填到详情</el-button>
      </template>
    </el-dialog>

    <!-- AI辅助补全结果弹窗 -->
    <el-dialog v-model="showAutoFillDialog" title="🤖 AI辅助补全" width="800px" :close-on-click-modal="false">
      <div v-loading="autofillLoading">
        <template v-if="autofillResult">
          <el-table :data="autofillFields" border stripe style="margin-bottom:16px">
            <el-table-column type="selection" width="45" />
            <el-table-column prop="label" label="字段" width="120" />
            <el-table-column prop="current" label="当前值" />
            <el-table-column prop="suggested" label="AI建议值">
              <template #default="{ row }">
                <span :style="{color: row.suggested !== row.current ? '#409eff' : '#999'}">{{ row.suggested || '-' }}</span>
              </template>
            </el-table-column>
          </el-table>
        </template>
        <el-empty v-else-if="!autofillLoading" description="点击生成补全建议" />
      </div>
      <template #footer>
        <el-button @click="fillSelectedAutoFill">回填选中字段</el-button>
        <el-button type="primary" @click="fillAllAutoFill">回填全部建议</el-button>
      </template>
    </el-dialog>

    <!-- 价格智能摸排弹窗 -->
    <el-dialog v-model="showPriceResearchDialog" title="💹 价格智能摸排" width="800px" :close-on-click-modal="false">
      <div v-loading="priceResearching">
        <template v-if="priceResearchResult">
          <el-alert
            :type="priceResearchResult.overall === 'REASONABLE' ? 'success' : priceResearchResult.overall === 'HIGH' ? 'warning' : 'info'"
            :closable="false"
            style="margin-bottom:16px"
          >
            <template #title>
              价格评分：{{ priceResearchResult.score }} 分 ·
              {{ priceResearchResult.overall === 'REASONABLE' ? '定价合理' : priceResearchResult.overall === 'HIGH' ? '定价偏高' : '定价偏低' }}
            </template>
          </el-alert>

          <el-row :gutter="16" style="margin-bottom:16px">
            <el-col :span="8">
              <el-card shadow="hover">
                <div style="text-align:center">
                  <div style="font-size:13px;color:#999">我的售价</div>
                  <div style="font-size:26px;font-weight:700;color:#1a237e">¥{{ form.price?.toFixed(2) }}</div>
                </div>
              </el-card>
            </el-col>
            <el-col :span="8">
              <el-card shadow="hover">
                <div style="text-align:center">
                  <div style="font-size:13px;color:#999">建议售价</div>
                  <div style="font-size:26px;font-weight:700;color:#4caf50">¥{{ priceResearchResult.suggestedPrice?.toFixed(2) }}</div>
                </div>
              </el-card>
            </el-col>
            <el-col :span="8">
              <el-card shadow="hover">
                <div style="text-align:center">
                  <div style="font-size:13px;color:#999">建议区间</div>
                  <div style="font-size:18px;font-weight:600;color:#666">¥{{ priceResearchResult.priceLower?.toFixed(2) }} ~ ¥{{ priceResearchResult.priceUpper?.toFixed(2) }}</div>
                </div>
              </el-card>
            </el-col>
          </el-row>

          <h4 style="margin:16px 0 8px;color:#333">各平台价格对比</h4>
          <el-table :data="priceResearchResult.competitors" border stripe size="small">
            <el-table-column prop="platform" label="平台" width="120" />
            <el-table-column prop="productName" label="商品名称" />
            <el-table-column label="平台售价" width="150">
              <template #default="{ row }">¥{{ row.price?.toFixed(2) }}</template>
            </el-table-column>
            <el-table-column label="与商城差价" width="150">
              <template #default="{ row }">
                <el-tag :type="form.price < row.price ? 'success' : 'danger'" size="small">
                  {{ form.price < row.price ? '低' : '高' }}{{ Math.abs(((row.price - form.price) / row.price * 100)).toFixed(1) }}%
                </el-tag>
              </template>
            </el-table-column>
          </el-table>

          <h4 style="margin:16px 0 8px;color:#333">多维度分析</h4>
          <div v-for="(item, idx) in priceResearchResult.items" :key="idx" style="margin-bottom:8px;padding:10px 12px;background:#f5f7fa;border-radius:6px">
            <div style="display:flex;align-items:center;gap:8px">
              <el-tag :type="item.passed ? 'success' : 'warning'" size="small">{{ item.passed ? '通过' : '注意' }}</el-tag>
              <span style="font-weight:500">{{ item.dimension }}</span>
            </div>
            <div style="font-size:13px;color:#666;margin-top:4px">{{ item.detail }}</div>
            <div v-if="item.suggestion" style="font-size:13px;color:#409eff;margin-top:2px">→ {{ item.suggestion }}</div>
          </div>

          <el-alert type="info" :closable="false" style="margin-top:12px" show-icon>
            <template #title>{{ priceResearchResult.summary }}</template>
          </el-alert>
        </template>
        <el-empty v-else description="暂无摸排结果" />
      </div>
      <template #footer>
        <el-button type="primary" @click="applySuggestedPrice" :disabled="!priceResearchResult?.suggestedPrice">套用建议售价</el-button>
      </template>
    </el-dialog>

    <!-- 商品卖点提炼弹窗 -->
    <el-dialog v-model="showSellingPointsDialog" title="✨ 商品卖点提炼 — 大模型文案生成" width="800px" :close-on-click-modal="false">
      <div v-loading="sellingPointsLoading">
        <template v-if="sellingPointsData">
          <div style="background:#f5f7fa;padding:12px;border-radius:6px;margin-bottom:16px">
            <div style="font-size:12px;color:#999;margin-bottom:4px">输入信息</div>
            <div style="font-size:13px;color:#333">{{ sellingPointsInput }}</div>
          </div>
          <div v-for="o in sellingPointsCards" :key="o.type" style="background:#f8f9ff;padding:16px;border-radius:8px;margin-bottom:12px;border-left:4px solid #1a237e">
            <h4 style="font-size:14px;color:#1a237e;margin-bottom:8px">{{ o.type }}</h4>
            <pre style="white-space:pre-wrap;font-size:13px;color:#333;font-family:inherit;line-height:1.8;margin:0">{{ o.content }}</pre>
          </div>
          <div v-if="sellingPointsData.highlights && sellingPointsData.highlights.length > 0" style="margin-top:12px">
            <span style="font-size:13px;color:#888">核心亮点：</span>
            <el-tag v-for="h in sellingPointsData.highlights" :key="h" type="info" style="margin:2px 4px">{{ h }}</el-tag>
          </div>
        </template>
        <el-empty v-else-if="!sellingPointsLoading" description="点击商品卖点提炼按钮生成文案" />
      </div>
      <template #footer>
        <el-button type="primary" @click="regenerateSellingPoints" :loading="sellingPointsLoading">重新生成</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import RichTextEditor from '../../components/RichTextEditor.vue'
import request from '../../utils/request'

const loading = ref(false)
const showModal = ref(false)
const showViewDialog = ref(false)
const showEditWithRejectDialog = ref(false)
const showBenefitModal = ref(false)
const benefitSubmitting = ref(false)
const benefitImageList = ref([])
const products = ref([])
const merchants = ref([])
const categories = ref([])
const imageList = ref([])
const editingProduct = ref(null)

const currentPage = ref(1)
const pageSize = ref(10)
const pagedProducts = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return products.value.slice(start, start + pageSize.value)
})

const productAuditLogs = computed(() => {
  const p = viewProductData.value
  if (!p) return []
  const logs = []
  let id = 0

  // 1. 商品提交
  if (p.createTime) {
    logs.push({
      id: id++,
      nodeName: '商品提交',
      time: p.createTime,
      operator: p.merchantName || '商户',
      result: '',
      comment: ''
    })
  }

  // 2. 一级审核
  if (p.level1AuditTime) {
    const isApproved = p.reviewLevel >= 1
    logs.push({
      id: id++,
      nodeName: '一级选品审核',
      time: p.level1AuditTime,
      operator: p.level1Auditor || '-',
      result: isApproved ? '通过' : '驳回',
      comment: p.rejectReason || p.approveReason || ''
    })
  }

  // 3. 二级审核
  if (p.level2AuditTime) {
    const isApproved = p.reviewLevel >= 2
    logs.push({
      id: id++,
      nodeName: '二级选品审核',
      time: p.level2AuditTime,
      operator: p.level2Auditor || '-',
      result: isApproved ? '通过' : '驳回',
      comment: p.rejectReason || p.approveReason || ''
    })
  }

  // 4. 如果只有旧版审核记录
  if (logs.length <= 1 && (p.auditTime || p.auditor || p.rejectReason)) {
    logs.push({
      id: id++,
      nodeName: '审核记录',
      time: p.auditTime || '-',
      operator: p.auditor || '-',
      result: p.rejectReason ? '驳回' : '通过',
      comment: p.rejectReason || ''
    })
  }

  return logs
})

const viewProductData = ref(null)
const detailTab = ref('basic')
const resubmitTarget = ref(null)

const form = ref({
  id: null,
  productCode: '',
  productName: '',
  merchantId: null,
  categoryId: null,
  brand: '',
  price: 0,
  marketPrice: 0,
  vipPrice: 0,
  stock: 0,
  productImage: '',
  imageUrls: '',
  description: '',
  detail: '',
  productType: 'PHYSICAL',
  isOnShelf: false,
  tags: '',
  benefitType: '',
  faceValue: 0,
  settlePrice: 0,
  validityType: '',
  validityDays: 0,
  validityStart: '',
  validityEnd: '',
  exchangeMethod: '',
  stockTotal: 0,
  stockDailyLimit: 0,
  stockPerUser: 0,
  supplierName: '',
  supplierContact: '',
  usageRules: '',
  applicableScope: '',
  detailDesc: '',
  benefitDescription: '',
  imageUrl: '',
  refundPolicy: ''
})

const benefitForm = ref({
  benefitName: '', benefitType: 'MEMBERSHIP', faceValue: 0, price: 0, settlePrice: 0,
  validityType: 'DAYS_AFTER_RECEIVE', validityDays: 30, validityStart: '', validityEnd: '',
  exchangeMethod: 'AUTO_BIND',
  stockTotal: 0, stockDailyLimit: 0, stockPerUser: 0,
  supplierId: null, supplierName: '', supplierContact: '',
  imageUrl: '', benefitDescription: '',
  usageRules: '', applicableScope: '', detailDesc: '', refundPolicy: 'NO_REFUND'
})

const productTypeMap = { PHYSICAL: '实物商品', VIRTUAL: '虚拟商品', BENEFIT: '权益商品', DIGITAL: '权益商品' }
const getProductTypeTag = (t) => {
  if (!t || t === 'PHYSICAL') return ''
  if (t === 'VIRTUAL') return 'warning'
  return 'success'
}
const getProductTypeText = (t) => t ? (productTypeMap[t] || t) : '实物商品'

const benefitTypeMap = { MEMBERSHIP: '会员权益', COUPON: '优惠券', GAME_POINTS: '游戏点卡', DIGITAL_CONTENT: '数字内容', SERVICE: '在线服务', INSURANCE: '保险/延保' }
const getBenefitTypeText = (t) => t ? (benefitTypeMap[t] || t) : '-'
const exchangeMethodMap = { AUTO_BIND: '自动绑定', CODE: '兑换码', QR_CODE: '二维码核销', MANUAL: '人工发放' }
const getExchangeMethodText = (m) => m ? (exchangeMethodMap[m] || m) : '-'
const validityTypeMap = { FIXED_DATE: '固定日期', DAYS_AFTER_RECEIVE: '领取后N天有效', DURATION: '长期有效' }
const getValidityTypeText = (v) => v ? (validityTypeMap[v] || v) : '-'
const refundPolicyMap = { NO_REFUND: '不可退款', CONDITIONAL: '有条件退款', FULL_REFUND: '支持退款' }
const getRefundPolicyText = (p) => p ? (refundPolicyMap[p] || p) : '-'

const getStatusType = (status) => {
  const types = { ON_SHELF: 'success', PENDING: 'warning', OFF_SHELF: 'info', REJECTED: 'danger' }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const map = { ON_SHELF: '在售', PENDING: '待上架', OFF_SHELF: '已下架', REJECTED: '已驳回' }
  return map[status] || status
}

const getAuditStatusType = (row) => {
  const status = row.status
  const types = { PENDING: 'warning', AUDITING: '', APPROVED: 'success', REJECTED: 'danger' }
  if (status === 'ON_SHELF' || status === 'OFF_SHELF') return 'success'
  return types[status] || 'info'
}

const getAuditStatusText = (row) => {
  const status = row.status
  const map = { AUDITING: '审核中', APPROVED: '已通过', REJECTED: '已驳回' }
  if (status === 'ON_SHELF' || status === 'OFF_SHELF') return '已通过'
  if (status === 'PENDING') {
    const reviewLevel = row.reviewLevel || 0
    if (reviewLevel >= 1) return '待二级选品审核'
    return '待一级选品审核'
  }
  return map[status] || status
}

const getFirstImage = (row) => {
  const urls = row?.imageUrls || row?.productImage
  if (!urls) return ''
  return urls.split(',')[0]
}

const openAddProduct = () => {
  editingProduct.value = null
  imageList.value = []
  form.value = {
    id: null,
    productCode: '',
    productName: '',
    merchantId: null,
    categoryId: null,
    brand: '',
    price: 0,
    marketPrice: 0,
    vipPrice: 0,
    stock: 0,
    productImage: '',
    imageUrls: '',
    description: '',
    detail: '',
    productType: 'PHYSICAL',
    isOnShelf: false,
    tags: '',
    benefitType: '',
    faceValue: 0,
    settlePrice: 0,
    validityType: '',
    validityDays: 0,
    exchangeMethod: '',
    stockTotal: 0,
    stockDailyLimit: 0,
    stockPerUser: 0,
    supplierName: '',
    supplierContact: '',
    usageRules: '',
    applicableScope: '',
    detailDesc: '',
    benefitDescription: '',
    imageUrl: '',
    refundPolicy: ''
  }
  showModal.value = true
}

const fetchProducts = async () => {
  try {
    const res = await request.get('/product/list')
    if (res.code === 200) {
      products.value = res.data?.list || res.data || []
    }
  } catch (e) {
    console.error('Failed to fetch products:', e)
    products.value = []
  }
}

const fetchMerchants = async () => {
  try {
    const res = await request.get('/merchant')
    if (res.code === 200) {
      merchants.value = (res.data?.list || res.data || []).filter(m => m.onboardingStatus === 'APPROVED')
    }
  } catch (e) {
    console.error('Failed to fetch merchants:', e)
    merchants.value = [
      { id: 1, merchantName: '瑞幸咖啡（中国）有限公司' },
      { id: 2, merchantName: '上海寻梦信息技术有限公司' },
      { id: 3, merchantName: '深圳腾讯计算机系统有限公司' },
      { id: 4, merchantName: '阿里巴巴（中国）有限公司' },
      { id: 5, merchantName: '爱奇艺（北京）科技有限公司' }
    ]
  }
}

const fetchCategories = async () => {
  try {
    const res = await request.get('/product/categories')
    if (res.code === 200) {
      categories.value = res.data || []
    }
  } catch (e) {
    console.error('Failed to fetch categories:', e)
    categories.value = [
      { id: 1, categoryName: '视频娱乐' },
      { id: 2, categoryName: '音乐音频' },
      { id: 3, categoryName: '本地生活' },
      { id: 4, categoryName: '电商会员' },
      { id: 5, categoryName: '游戏充值' },
      { id: 6, categoryName: '话费充值' }
    ]
  }
}

const handleImageUpload = (response, file, fileList) => {
  if (response.code === 200) {
    const url = response.data
    const existing = form.value.imageUrls ? form.value.imageUrls.split(',').filter(u => u) : []
    existing.push(url)
    form.value.imageUrls = existing.join(',')
    imageList.value = existing.map((u, i) => ({
      uid: Date.now() + i,
      name: `图片${i + 1}`,
      url: u
    }))
  }
}

const handleImageRemove = (file, fileList) => {
  const url = file.url || file.response?.data
  const existing = form.value.imageUrls ? form.value.imageUrls.split(',').filter(u => u && u !== url) : []
  form.value.imageUrls = existing.join(',')
  imageList.value = existing.map((u, i) => ({
    uid: Date.now() + i,
    name: `图片${i + 1}`,
    url: u
  }))
}

const handleBenefitImageUpload = (response, file, fileList) => {
  if (response.code === 200) {
    benefitForm.value.imageUrl = response.data
    benefitImageList.value = [{ uid: Date.now(), name: '商品图片', url: response.data }]
  }
}

const handleBenefitImageRemove = () => {
  benefitForm.value.imageUrl = ''
  benefitImageList.value = []
}

const editProduct = async (product) => {
  editingProduct.value = product
  const imgUrls = product.imageUrls || ''
  form.value = {
    id: product.id,
    productCode: product.productCode || '',
    productName: product.productName || '',
    merchantId: product.merchantId || null,
    categoryId: product.categoryId || null,
    brand: product.brand || '',
    price: product.price || 0,
    marketPrice: product.marketPrice || 0,
    vipPrice: product.vipPrice || 0,
    stock: product.stock || 0,
    productImage: product.productImage || '',
    imageUrls: imgUrls,
    description: product.description || '',
    detail: product.detail || '',
    productType: product.productType || 'PHYSICAL',
    isOnShelf: product.status === 'ON_SHELF',
    tags: product.tags || '',
    benefitType: '',
    faceValue: 0,
    settlePrice: 0,
    validityType: '',
    validityDays: 0,
    exchangeMethod: '',
    stockTotal: 0,
    stockDailyLimit: 0,
    stockPerUser: 0,
    supplierName: '',
    supplierContact: '',
    usageRules: '',
    applicableScope: '',
    detailDesc: '',
    benefitDescription: '',
    imageUrl: '',
    refundPolicy: ''
  }
  imageList.value = imgUrls ? imgUrls.split(',').filter(u => u).map((u, i) => ({
    uid: Date.now() + i,
    name: `图片${i + 1}`,
    url: u
  })) : []
  if (product.productType === 'BENEFIT') {
    await fetchBenefitData(product.productName)
  }
  showModal.value = true
}

const viewProduct = async (product) => {
  detailTab.value = 'basic'
  viewProductData.value = product
  if (product.productType === 'BENEFIT') {
    try {
      const res = await request.get(`/benefit/by-name/${encodeURIComponent(product.productName)}`)
      if (res.code === 200 && res.data) {
        viewProductData.value = { ...product, _benefit: res.data }
      }
    } catch (e) {
      console.error('Failed to fetch benefit data:', e)
    }
  }
  showViewDialog.value = true
}

const toggleShelf = async (product) => {
  try {
    const newStatus = product.status === 'ON_SHELF' ? 'OFF_SHELF' : 'ON_SHELF'
    const res = await request.put(`/product/${product.id}`, { status: newStatus })
    if (res.code === 200) {
      ElMessage.success(newStatus === 'ON_SHELF' ? '上架成功' : '下架成功')
      await fetchProducts()
    }
  } catch (e) {
    console.error('Toggle shelf error:', e)
    ElMessage.error('操作失败，请稍后重试')
  }
}

const resubmitProduct = async (product) => {
  resubmitTarget.value = product
  editingProduct.value = product
  const imgUrls = product.imageUrls || ''
  form.value = {
    id: product.id,
    productCode: product.productCode || '',
    productName: product.productName || '',
    merchantId: product.merchantId || null,
    categoryId: product.categoryId || null,
    brand: product.brand || '',
    price: product.price || 0,
    marketPrice: product.marketPrice || 0,
    vipPrice: product.vipPrice || 0,
    stock: product.stock || 0,
    productImage: product.productImage || '',
    imageUrls: imgUrls,
    description: product.description || '',
    detail: product.detail || '',
    productType: product.productType || 'PHYSICAL',
    isOnShelf: false,
    tags: product.tags || '',
    benefitType: '',
    faceValue: 0,
    settlePrice: 0,
    validityType: '',
    validityDays: 0,
    exchangeMethod: '',
    stockTotal: 0,
    stockDailyLimit: 0,
    stockPerUser: 0,
    supplierName: '',
    supplierContact: '',
    usageRules: '',
    applicableScope: '',
    detailDesc: '',
    benefitDescription: '',
    imageUrl: '',
    refundPolicy: ''
  }
  imageList.value = imgUrls ? imgUrls.split(',').filter(u => u).map((u, i) => ({
    uid: Date.now() + i,
    name: `图片${i + 1}`,
    url: u
  })) : []
  if (product.productType === 'BENEFIT') {
    await fetchBenefitData(product.productName)
  }
  showEditWithRejectDialog.value = true
}

const submitResubmit = async () => {
  if (!form.value.productName || !form.value.merchantId) {
    ElMessage.warning('请填写必填项')
    return
  }
  const postData = { ...form.value, status: 'PENDING' }
  delete postData.isOnShelf
  try {
    const res = await request.put(`/product/${form.value.id}`, postData)
    if (res.code === 200) {
      ElMessage.success('商品已重新提交，等待审核')
      showEditWithRejectDialog.value = false
      resubmitTarget.value = null
      await fetchProducts()
    }
  } catch (e) {
    console.error('Resubmit product error:', e)
    ElMessage.error('重新提交失败，请稍后重试')
  }
}

const submitProduct = async () => {
  if (!form.value.productName || !form.value.merchantId) {
    ElMessage.warning('请填写必填项')
    return
  }
  const postData = { ...form.value }
  delete postData.isOnShelf
  try {
    if (editingProduct.value) {
      const res = await request.put(`/product/${form.value.id}`, postData)
      if (res.code === 200) {
        ElMessage.success('商品更新成功')
        showModal.value = false
        await fetchProducts()
      }
    } else {
      const res = await request.post('/product', postData)
      if (res.code === 200) {
        ElMessage.success('商品已提交，等待审核')
        showModal.value = false
        await fetchProducts()
      }
    }
  } catch (e) {
    console.error('Submit product error:', e)
    ElMessage.error('提交失败，请稍后重试')
  }
}

// ==================== AI Proofreading ====================
const proofreading = ref(false)
const proofreadTarget = ref('description')
const proofreadResult = ref(null)
const showProofreadDialog = ref(false)
const proofreadLoading = ref(false)

const doProofread = async (target = 'description') => {
  let content
  if (target === 'description') content = form.value.description
  else if (target === 'detail') content = form.value.detail
  else if (target === 'benefitDescription') content = form.value.benefitDescription
  else content = form.value.description
  if (!content) { ElMessage.warning('请输入内容后再校对'); return }
  proofreadTarget.value = target
  proofreading.value = true
  proofreadResult.value = null
  showProofreadDialog.value = true
  proofreadLoading.value = true
  try {
    const res = await request.post('/ai/proofread', { target, content })
    if (res.code === 200) {
      const raw = (res.data?.raw || '').trim()
      if (!raw) {
        proofreadResult.value = null
        ElMessage.warning('AI校对暂无结果')
      } else {
        try {
          proofreadResult.value = JSON.parse(raw.replace(/```json\s*|```/g, '').trim())
          if (!proofreadResult.value.optimizedContent && !proofreadResult.value.summary) {
            proofreadResult.value = { optimizedContent: raw, issues: [], summary: '' }
          }
        } catch {
          proofreadResult.value = { optimizedContent: raw, issues: [], summary: '' }
        }
      }
    } else {
      proofreadResult.value = null
      ElMessage.warning(res.message || 'AI校对暂无结果')
    }
  } catch (e) {
    console.error('Proofread error:', e)
    proofreadResult.value = null
    ElMessage.error('AI校对失败')
  } finally {
    proofreading.value = false
    proofreadLoading.value = false
  }
}

const fillProofreadResult = () => {
  if (proofreadResult.value?.optimizedContent) {
    if (proofreadTarget.value === 'description') form.value.description = proofreadResult.value.optimizedContent
    else if (proofreadTarget.value === 'detail') form.value.detail = proofreadResult.value.optimizedContent
    else if (proofreadTarget.value === 'benefitDescription') form.value.benefitDescription = proofreadResult.value.optimizedContent
    showProofreadDialog.value = false
    ElMessage.success('已回填')
  }
}

// ==================== AI Autofill ====================
const autofilling = ref(false)
const autofillResult = ref(null)
const showAutoFillDialog = ref(false)
const autofillLoading = ref(false)
const autofillFields = ref([])

const stripHtml = (html) => {
  if (!html) return ''
  return html.replace(/<[^>]+>/g, '').substring(0, 100)
}

const doAutoFill = async () => {
  if (!form.value.productName) { ElMessage.warning('请先输入商品名称'); return }
  autofilling.value = true
  autofillResult.value = null
  autofillFields.value = []
  showAutoFillDialog.value = true
  autofillLoading.value = true
  try {
    const res = await request.post('/ai/autofill', {
      productName: form.value.productName,
      productType: form.value.productType,
      description: form.value.description,
      detail: form.value.detail
    })
    if (res.code === 200) {
      const raw = (res.data?.raw || '').trim()
      let parsed = {}
      if (raw) {
        try {
          parsed = JSON.parse(raw.replace(/```json\s*|```/g, '').trim())
        } catch {
          parsed = {}
        }
      }
      autofillResult.value = parsed
      autofillFields.value = [
        { key: 'productName', label: '商品名称', current: form.value.productName || '', suggested: parsed.productName || '' },
        { key: 'description', label: '商品介绍', current: form.value.description || '', suggested: parsed.description || '' },
        { key: 'detail', label: '商品详情', current: stripHtml(form.value.detail || ''), suggested: parsed.detail || '' },
        { key: 'price', label: '售价', current: form.value.price ? '¥' + form.value.price : '', suggested: parsed.suggestedPrice ? '¥' + parsed.suggestedPrice : '' },
        { key: 'tags', label: '卖点标签', current: form.value.tags || '', suggested: Array.isArray(parsed.tags) ? parsed.tags.join(',') : (parsed.tags || '') },
        { key: 'categorySuggestion', label: '分类建议', current: '-', suggested: parsed.categorySuggestion || '' }
      ]
    } else {
      showAutoFillDialog.value = false
      autofillResult.value = null
      ElMessage.warning(res.message || 'AI补全暂无结果')
    }
  } catch (e) {
    console.error('Autofill error:', e)
    showAutoFillDialog.value = false
    autofillResult.value = null
    ElMessage.error('AI补全失败')
  } finally {
    autofilling.value = false
    autofillLoading.value = false
  }
}

const fillSelectedAutoFill = () => {
  ElMessage.info('请在弹窗内勾选要回填的行，暂不支持多选回填。请使用"回填全部建议"')
}

const fillAllAutoFill = () => {
  if (!autofillResult.value) return
  const r = autofillResult.value
  if (r.productName) form.value.productName = r.productName
  if (r.description) form.value.description = r.description
  if (r.detail) form.value.detail = r.detail
  if (r.suggestedPrice) form.value.price = r.suggestedPrice
  if (r.tags) form.value.tags = Array.isArray(r.tags) ? r.tags.join(',') : r.tags
  showAutoFillDialog.value = false
  ElMessage.success('已回填全部建议')
}

// ==================== Price Research ====================
const priceResearching = ref(false)
const priceResearchResult = ref(null)
const showPriceResearchDialog = ref(false)

const doPriceResearch = async () => {
  if (!form.value.productName) { ElMessage.warning('请先输入商品名称'); return }
  priceResearching.value = true
  priceResearchResult.value = null
  showPriceResearchDialog.value = true
  try {
    const res = await request.post('/ai/price-research', {
      productName: form.value.productName,
      productType: form.value.productType,
      price: form.value.price,
      marketPrice: form.value.marketPrice
    })
    if (res.code === 200) {
      priceResearchResult.value = res.data
    } else {
      ElMessage.warning(res.message || '价格摸排暂无结果')
    }
  } catch (e) {
    console.error('Price research error:', e)
    ElMessage.error('价格摸排失败')
  } finally {
    priceResearching.value = false
  }
}

const applySuggestedPrice = () => {
  if (priceResearchResult.value?.suggestedPrice) {
    form.value.price = priceResearchResult.value.suggestedPrice
    showPriceResearchDialog.value = false
    ElMessage.success('已套用建议售价 ¥' + priceResearchResult.value.suggestedPrice.toFixed(2))
  }
}

// ==================== Selling Points ====================
const sellingPointsLoading = ref(false)
const sellingPointsData = ref(null)
const sellingPointsInput = ref('')
const showSellingPointsDialog = ref(false)

const extractSellingPoints = async (row) => {
  sellingPointsInput.value = [
    `商品名称: ${row.productName}`,
    `商品类型: ${getProductTypeText(row.productType)}`,
    `价格: ¥${(row.price || 0).toFixed(2)}`,
    `品牌: ${row.brand || '-'}`,
    `商品介绍: ${row.description || '-'}`
  ].join('\n')
  sellingPointsData.value = null
  showSellingPointsDialog.value = true
  sellingPointsLoading.value = true
  try {
    const res = await request.post('/ai/selling-points', {
      productName: row.productName,
      productType: row.productType,
      price: row.price,
      description: row.description,
      brand: row.brand,
      category: row.categoryName || row.category || ''
    })
    if (res.code === 200) {
      const raw = (res.data?.raw || '').trim()
      if (raw) {
        try {
          sellingPointsData.value = JSON.parse(raw.replace(/```json\s*|```/g, '').trim())
        } catch {
          sellingPointsData.value = { shortTitle: raw }
        }
      }
    }
  } catch (e) {
    console.error('Selling points error:', e)
  } finally {
    sellingPointsLoading.value = false
  }
}

const sellingPointsCards = computed(() => {
  if (!sellingPointsData.value) return []
  const cards = []
  if (sellingPointsData.value.shortTitle) cards.push({ type: '🏷️ 短标题', content: sellingPointsData.value.shortTitle })
  if (sellingPointsData.value.corePoints) cards.push({ type: '💡 核心卖点', content: sellingPointsData.value.corePoints })
  if (sellingPointsData.value.marketingCopy) cards.push({ type: '📢 营销文案', content: sellingPointsData.value.marketingCopy })
  if (sellingPointsData.value.socialCopy) cards.push({ type: '📱 社交分享文案', content: sellingPointsData.value.socialCopy })
  return cards
})

const regenerateSellingPoints = () => {
  if (!sellingPointsInput.value) return
  sellingPointsLoading.value = true
  extractSellingPoints({ productName: '' })
}

// ==================== Benefit Introduction ====================
const openBenefitDialog = () => {
  benefitForm.value = {
    benefitName: '', benefitType: 'MEMBERSHIP', faceValue: 0, price: 0, settlePrice: 0,
    validityType: 'DAYS_AFTER_RECEIVE', validityDays: 30, validityStart: '', validityEnd: '',
    exchangeMethod: 'AUTO_BIND', stockTotal: 0, stockDailyLimit: 0, stockPerUser: 0,
    supplierId: null, supplierName: '', supplierContact: '',
    imageUrl: '', benefitDescription: '',
    usageRules: '', applicableScope: '', detailDesc: '', refundPolicy: 'NO_REFUND'
  }
  benefitImageList.value = []
  showBenefitModal.value = true
}

const onSupplierChange = (supplierId) => {
  if (!supplierId) {
    benefitForm.value.supplierName = ''
    benefitForm.value.supplierContact = ''
    return
  }
  const supplier = merchants.value.find(m => m.id === supplierId)
  if (supplier) {
    benefitForm.value.supplierName = supplier.merchantName || ''
    benefitForm.value.supplierContact = supplier.contactPhone || supplier.contactName || ''
  }
}

const submitBenefit = async () => {
  if (!benefitForm.value.benefitName) { ElMessage.warning('请输入权益名称'); return }
  benefitSubmitting.value = true
  try {
    const postData = { ...benefitForm.value, merchantId: benefitForm.value.supplierId, productType: 'BENEFIT', status: 'PENDING' }
    const res = await request.post('/benefit', postData)
    if (res.code === 200) {
      ElMessage.success('权益引入已提交')
      showBenefitModal.value = false
      await fetchProducts()
    }
  } catch (e) {
    console.error('Submit benefit error:', e)
    ElMessage.error('提交失败')
  } finally {
    benefitSubmitting.value = false
  }
}

const fetchBenefitData = async (productName) => {
  try {
    const res = await request.get(`/benefit/by-name/${encodeURIComponent(productName)}`)
    if (res.code === 200 && res.data) {
      const b = res.data
      Object.assign(form.value, {
        benefitType: b.benefitType || '',
        faceValue: b.faceValue || 0,
        settlePrice: b.settlePrice || 0,
        validityType: b.validityType || '',
        validityDays: b.validityDays || 0,
        validityStart: b.validityStart || '',
        validityEnd: b.validityEnd || '',
        exchangeMethod: b.exchangeMethod || '',
        stockTotal: b.stockTotal || 0,
        stockDailyLimit: b.stockDailyLimit || 0,
        stockPerUser: b.stockPerUser || 0,
        supplierName: b.supplierName || '',
        supplierContact: b.supplierContact || '',
        usageRules: b.usageRules || '',
        applicableScope: b.applicableScope || '',
        detailDesc: b.detailDesc || '',
        benefitDescription: b.benefitDescription || '',
        refundPolicy: b.refundPolicy || ''
      })
    }
  } catch (e) {
    console.error('Failed to fetch benefit data:', e)
  }
}

onMounted(async () => {
  await Promise.all([fetchProducts(), fetchMerchants(), fetchCategories()])
})
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.no-image {
  color: #ccc;
  font-size: 12px;
}

.audit-section {
  margin-top: 16px;
}

.table-container {
  margin-top: 8px;
}
</style>