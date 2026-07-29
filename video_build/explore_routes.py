import os, asyncio
from playwright.async_api import async_playwright

OUT = "/Users/wangwei/WorkBuddy/2026-07-23-09-20-10/mall-ecosystem/video_build/explore"
os.makedirs(OUT, exist_ok=True)

async def login_admin(page):
    await page.goto("http://localhost:5173/login", wait_until="networkidle", timeout=30000)
    await page.wait_for_timeout(800)
    # fill first text/password inputs
    await page.locator('input[type="text"]').first.fill("admin")
    await page.locator('input[type="password"]').first.fill("admin123")
    await page.locator('button:has-text("登")').first.click()
    await page.wait_for_timeout(2500)

async def screenshot(page, name):
    await page.screenshot(path=os.path.join(OUT, name), full_page=False)
    print(f"  -> {name}")

async def try_click_menu(page, text):
    try:
        loc = page.locator('.el-menu-item').filter(has_text=text)
        if await loc.count() > 0:
            await loc.first.click()
            await page.wait_for_timeout(1500)
            return True
        # try exact text
        loc2 = page.locator(f'text={text}')
        if await loc2.count() > 0:
            await loc2.first.click()
            await page.wait_for_timeout(1500)
            return True
    except Exception as e:
        print(f"menu click failed {text}: {e}")
    return False

async def main():
    async with async_playwright() as p:
        browser = await p.chromium.launch(channel="chrome", headless=True, args=["--no-sandbox", "--disable-gpu"])
        page = await browser.new_page(viewport={"width": 1920, "height": 1080}, device_scale_factor=1)
        await login_admin(page)
        await screenshot(page, "admin_1_platform_select.png")
        # click first platform card then enter
        await page.locator('text=生态合作平台').first.click()
        await page.wait_for_timeout(1000)
        await page.locator('button:has-text("进入平台")').first.click()
        await page.wait_for_timeout(2500)
        await screenshot(page, "admin_2_dashboard.png")
        print(f"Dashboard URL: {page.url}")
        # enumerate menu texts
        menus = await page.locator('.el-menu-item, .el-sub-menu__title').all_inner_texts()
        print("Menus:", [m.strip() for m in menus if m.strip()])
        # click key menus
        for menu in ["首页", "商户管理", "商品管理", "订单管理", "结算管理", "AI+应用", "系统管理"]:
            ok = await try_click_menu(page, menu)
            if ok:
                await screenshot(page, f"admin_menu_{menu}.png")
            else:
                print(f"  menu not found: {menu}")
        await browser.close()

asyncio.run(main())
