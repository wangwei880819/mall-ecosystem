import os, asyncio, json
from playwright.async_api import async_playwright

OUT = "/Users/wangwei/WorkBuddy/2026-07-23-09-20-10/mall-ecosystem/video_build"
os.makedirs(OUT, exist_ok=True)

async def main():
    async with async_playwright() as p:
        browser = await p.chromium.launch(channel="chrome", headless=True, args=["--no-sandbox", "--disable-gpu"])
        page = await browser.new_page(viewport={"width": 1920, "height": 1080}, device_scale_factor=1)
        # login
        await page.goto("http://localhost:5173/login", wait_until="networkidle", timeout=30000)
        await page.wait_for_timeout(800)
        await page.locator('input[type="text"]').first.fill("admin")
        await page.locator('input[type="password"]').first.fill("admin123")
        await page.locator('button:has-text("登")').first.click()
        await page.wait_for_timeout(2500)
        # enter platform
        await page.locator('text=生态合作平台').first.click()
        await page.wait_for_timeout(800)
        await page.locator('button:has-text("进入平台")').first.click()
        await page.wait_for_timeout(2500)
        print("URL after enter:", page.url)

        # Dump menu structure: sub-menu titles and their child items with hrefs
        result = {}
        # submenu titles
        sub_titles = await page.locator('.el-sub-menu__title').all_inner_texts()
        print("SUBMENU TITLES:", [s.strip() for s in sub_titles])
        # all menu items (leaf) with href
        leaves = page.locator('.el-menu-item')
        n = await leaves.count()
        print(f"LEAF MENU ITEMS: {n}")
        items = []
        for i in range(n):
            loc = leaves.nth(i)
            txt = (await loc.inner_text()).strip()
            href = ""
            try:
                href = await loc.locator('a').first.get_attribute('href') or ""
            except Exception:
                href = ""
            items.append({"text": txt, "href": href})
        print("LEAVES:")
        for it in items:
            print(f"  {it['text']!r} -> {it['href']}")
        result["leaves"] = items

        # current path
        result["current_url"] = page.url

        with open(os.path.join(OUT, "menu_probe.json"), "w", encoding="utf-8") as f:
            json.dump(result, f, ensure_ascii=False, indent=2)
        await browser.close()

asyncio.run(main())
