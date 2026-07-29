import os, asyncio
from playwright.async_api import async_playwright

OUT = "/Users/wangwei/WorkBuddy/2026-07-23-09-20-10/mall-ecosystem/video_build/explore"
os.makedirs(OUT, exist_ok=True)

async def login_admin(page):
    await page.goto("http://localhost:5173/login", wait_until="networkidle", timeout=30000)
    await page.wait_for_timeout(800)
    await page.locator('input[type="text"]').first.fill("admin")
    await page.locator('input[type="password"]').first.fill("admin123")
    await page.locator('button:has-text("登")').first.click()
    await page.wait_for_timeout(2000)
    await page.locator('text=生态合作平台').first.click()
    await page.wait_for_timeout(800)
    await page.locator('button:has-text("进入平台")').first.click()
    await page.wait_for_timeout(2000)

async def click_text(page, text, nth=0):
    loc = page.locator('.el-menu-item').filter(has_text=text)
    if await loc.count() > nth:
        await loc.nth(nth).click()
        await page.wait_for_timeout(1800)
        return True
    # fallback exact text anywhere clickable
    loc2 = page.locator(f'text={text}')
    if await loc2.count() > 0:
        await loc2.first.click()
        await page.wait_for_timeout(1800)
        return True
    return False

async def screenshot(page, name):
    await page.screenshot(path=os.path.join(OUT, name), full_page=False)
    print(f"  -> {name} url={page.url}")

async def main():
    async with async_playwright() as p:
        browser = await p.chromium.launch(channel="chrome", headless=True, args=["--no-sandbox", "--disable-gpu"])
        # admin context
        page = await browser.new_page(viewport={"width": 1920, "height": 1080}, device_scale_factor=1)
        await login_admin(page)
        # merchant subpages
        for txt in ["资质审核", "业务复审", "合规终审", "合同签署", "支付进件"]:
            if await click_text(page, txt):
                await screenshot(page, f"admin_{txt}.png")
        # product subpages
        for txt in ["商品列表", "商品审核", "权益引入"]:
            if await click_text(page, txt):
                await screenshot(page, f"admin_{txt}.png")
        # order / finance
        for txt in ["订单评价", "评价管理", "结算管理", "财务结算", "佣金结算"]:
            if await click_text(page, txt):
                await screenshot(page, f"admin_{txt}.png")
        # system mgmt
        for txt in ["接入平台管理"]:
            if await click_text(page, txt):
                await screenshot(page, f"admin_{txt}.png")

        # risk context
        page2 = await browser.new_page(viewport={"width": 1920, "height": 1080}, device_scale_factor=1)
        await page2.goto("http://localhost:3001/#/dashboard", wait_until="networkidle", timeout=30000)
        await page2.wait_for_timeout(2000)
        await page2.screenshot(path=os.path.join(OUT, "risk_dashboard.png"), full_page=False)
        for txt in ["规则管理", "风控事件", "名单库", "处置管理"]:
            if await click_text(page2, txt):
                await screenshot(page2, f"risk_{txt}.png")

        # customer context
        page3 = await browser.new_page(viewport={"width": 1920, "height": 1080}, device_scale_factor=1)
        await page3.goto("http://localhost:3000/", wait_until="networkidle", timeout=30000)
        await page3.wait_for_timeout(1500)
        await page3.screenshot(path=os.path.join(OUT, "customer_home.png"), full_page=False)
        # click first product card
        try:
            await page3.locator('.product-card, .goods-card, .card, [class*="product"]').first.click()
            await page3.wait_for_timeout(2000)
            await page3.screenshot(path=os.path.join(OUT, "customer_detail.png"), full_page=False)
        except Exception as e:
            print("customer product click failed", e)

        await browser.close()

asyncio.run(main())
