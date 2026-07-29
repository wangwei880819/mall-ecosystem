import os, asyncio, json
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

        # Find top-level nav items (those with ▶ expandable). Use nav>div structure.
        # Click each parent submenu, then dump child nav links found after expansion.
        parents = ["商户管理", "客户管理", "商品管理", "订单管理", "财务管理", "风险管理", "系统管理"]
        tree = {}
        for parent in parents:
            # click the parent nav item by text
            try:
                loc = page.locator('nav').locator('div, span, a').filter(has_text=parent)
                # more precise: find element whose text == parent (with emoji). Use get_by_text
                await page.get_by_text(parent, exact=False).first.click()
                await page.wait_for_timeout(900)
            except Exception as e:
                print(f"click parent {parent} failed: {e}")
            # dump all anchors with href starting with /
            links = await page.locator('nav a[href]').all()
            children = []
            for a in links:
                href = await a.get_attribute('href')
                txt = (await a.inner_text()).strip().replace("\n", " ")
                children.append({"text": txt, "href": href})
            # dedupe
            uniq = []
            for c in children:
                if c not in uniq:
                    uniq.append(c)
            tree[parent] = uniq
            print(f"\n{parent}:")
            for c in uniq:
                print(f"   {c['text']!r} -> {c['href']}")

        with open(os.path.join(OUT, "menu_tree.json"), "w", encoding="utf-8") as f:
            json.dump(tree, f, ensure_ascii=False, indent=2)
        await browser.close()

asyncio.run(main())
