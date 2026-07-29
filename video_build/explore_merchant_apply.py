import os, asyncio
from playwright.async_api import async_playwright

OUT = "/Users/wangwei/WorkBuddy/2026-07-23-09-20-10/mall-ecosystem/video_build/explore"
os.makedirs(OUT, exist_ok=True)

async def main():
    async with async_playwright() as p:
        browser = await p.chromium.launch(channel="chrome", headless=True, args=["--no-sandbox", "--disable-gpu"])
        page = await browser.new_page(viewport={"width": 1920, "height": 1080}, device_scale_factor=1)
        await page.goto("http://localhost:3002/#/login", wait_until="networkidle", timeout=30000)
        await page.wait_for_timeout(1500)
        await page.screenshot(path=os.path.join(OUT, "merchant_login.png"), full_page=False)
        # click 申请入驻
        try:
            await page.locator('text=申请入驻').first.click()
            await page.wait_for_timeout(2000)
            await page.screenshot(path=os.path.join(OUT, "merchant_apply.png"), full_page=False)
            print(f"Apply URL: {page.url}")
        except Exception as e:
            print("申请入驻 click failed", e)
        await browser.close()

asyncio.run(main())
