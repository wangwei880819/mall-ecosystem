import os, asyncio
from playwright.async_api import async_playwright

OUT = "/Users/wangwei/WorkBuddy/2026-07-23-09-20-10/mall-ecosystem/video_build"

async def main():
    async with async_playwright() as p:
        browser = await p.chromium.launch(channel="chrome", headless=True, args=["--no-sandbox", "--disable-gpu"])
        page = await browser.new_page(viewport={"width": 1920, "height": 1080}, device_scale_factor=1)
        await page.goto("http://localhost:5173/login", wait_until="networkidle", timeout=30000)
        await page.wait_for_timeout(800)
        await page.locator('input[type="text"]').first.fill("admin")
        await page.locator('input[type="password"]').first.fill("admin123")
        await page.locator('button:has-text("登")').first.click()
        await page.wait_for_timeout(2500)
        await page.locator('text=生态合作平台').first.click()
        await page.wait_for_timeout(800)
        await page.locator('button:has-text("进入平台")').first.click()
        await page.wait_for_timeout(3500)

        # Dump info
        print("URL:", page.url)
        # Look for any menu-like elements
        for cls in ["menu", "nav", "sidebar", "aside"]:
            locs = page.locator(f'[class*="{cls}"]')
            n = await locs.count()
            print(f"elements with class containing '{cls}': {n}")

        # print body text (first 1500 chars)
        body = await page.locator('body').inner_text()
        print("BODY TEXT (first 2500):")
        print(body[:2500])

        # list all <a href> and router-link
        links = await page.locator('a[href]').all()
        print(f"\nANCHOR LINKS: {len(links)}")
        seen = set()
        for a in links:
            href = await a.get_attribute('href')
            txt = (await a.inner_text()).strip()
            key = (href, txt)
            if key in seen: continue
            seen.add(key)
            if txt or (href and href.startswith('/')):
                print(f"  {txt[:30]!r} -> {href}")
        await browser.close()

asyncio.run(main())
