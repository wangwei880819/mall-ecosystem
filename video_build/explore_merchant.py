import os, asyncio
from playwright.async_api import async_playwright

OUT = "/Users/wangwei/WorkBuddy/2026-07-23-09-20-10/mall-ecosystem/video_build/explore"
os.makedirs(OUT, exist_ok=True)

TARGETS = [
    ("3002_merchant", "http://localhost:3002/"),
    ("3003_other", "http://localhost:3003/"),
]

async def main():
    async with async_playwright() as p:
        browser = await p.chromium.launch(channel="chrome", headless=True, args=["--no-sandbox", "--disable-gpu"])
        page = await browser.new_page(viewport={"width": 1920, "height": 1080}, device_scale_factor=1)
        for name, url in TARGETS:
            try:
                await page.goto(url, wait_until="networkidle", timeout=30000)
                await page.wait_for_timeout(3000)
                path = os.path.join(OUT, f"{name}.png")
                await page.screenshot(path=path, full_page=False)
                title = await page.title()
                print(f"OK {name} -> url={page.url} title={title}")
            except Exception as e:
                print(f"ERR {name}: {e}")
        await browser.close()

asyncio.run(main())
