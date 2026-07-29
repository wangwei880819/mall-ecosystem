# -*- coding: utf-8 -*-
import os, asyncio, sys
from playwright.async_api import async_playwright
sys.path.insert(0, os.path.dirname(__file__))
from shots_data import SHOTS

BASE = "/Users/wangwei/WorkBuddy/2026-07-23-09-20-10/mall-ecosystem/video_build"
SHOTS_DIR = os.path.join(BASE, "shots")
os.makedirs(SHOTS_DIR, exist_ok=True)

ADMIN = "http://localhost:5173"

async def login(page):
    await page.goto(ADMIN + "/login", wait_until="networkidle", timeout=30000)
    await page.wait_for_timeout(800)
    await page.locator('input[type="text"]').first.fill("admin")
    await page.locator('input[type="password"]').first.fill("admin123")
    await page.locator('button:has-text("登")').first.click()
    await page.wait_for_timeout(2500)

async def enter_platform(page):
    await page.locator('text=生态合作平台').first.click()
    await page.wait_for_timeout(800)
    await page.locator('button:has-text("进入平台")').first.click()
    await page.wait_for_timeout(3000)

async def drill(page):
    # click first data row / first primary table row
    for sel in ['tbody tr', '.el-table__row', '[class*="table"] tr', 'tr']:
        loc = page.locator(sel).first
        try:
            if await loc.count() > 0 and await loc.is_visible():
                await loc.click()
                await page.wait_for_timeout(1800)
                return True
        except Exception:
            pass
    return False

async def shot(page, name):
    path = os.path.join(SHOTS_DIR, name)
    await page.screenshot(path=path, full_page=False)
    print(f"  [shot] {name}  url={page.url}")

async def main():
    async with async_playwright() as p:
        browser = await p.chromium.launch(channel="chrome", headless=True, args=["--no-sandbox","--disable-gpu"])
        admin = await browser.new_page(viewport={"width":1920,"height":1080}, device_scale_factor=1)
        risk  = await browser.new_page(viewport={"width":1920,"height":1080}, device_scale_factor=1)
        merch = await browser.new_page(viewport={"width":1920,"height":1080}, device_scale_factor=1)
        cust  = await browser.new_page(viewport={"width":1920,"height":1080}, device_scale_factor=1)

        entered = False
        only = os.environ.get("ONLY")
        only_set = set(only.split(",")) if only else None
        for s in SHOTS:
            if only_set and s["id"] not in only_set:
                continue
            if s["type"] == "title":
                print(f"[skip title] {s['id']} {s['title']}")
                continue
            side = s["side"]
            try:
                if side == "admin":
                    if s["route"] == "/login":
                        await admin.goto(ADMIN + "/login", wait_until="networkidle")
                        await admin.wait_for_timeout(1200)
                        await shot(admin, s["file"]); continue
                    if s["route"] == "/portal_after_login":
                        await login(admin)
                        await admin.wait_for_timeout(800)
                        await shot(admin, s["file"]); continue
                    if not entered:
                        if s["route"] == "/portal":
                            # need to enter platform first
                            await enter_platform(admin)
                            entered = True
                        else:
                            # ensure logged in + entered
                            await login(admin)
                            await enter_platform(admin)
                            entered = True
                    if s["route"] == "/portal":
                        await admin.goto(ADMIN + "/portal", wait_until="domcontentloaded")
                        await admin.wait_for_timeout(1500)
                    else:
                        await admin.goto(ADMIN + s["route"], wait_until="domcontentloaded")
                        await admin.wait_for_timeout(1800)
                        # AI capability card demo
                        if s.get("card"):
                            try:
                                await admin.get_by_text(s["card"], exact=False).first.click(timeout=5000)
                                await admin.wait_for_timeout(1500)
                                # click inner demo button (演示/开始/生成)
                                for btxt in ["点击演示","开始识别","开始校对","开始摸排","生成","开始"]:
                                    try:
                                        await admin.locator('button:has-text("'+btxt+'")').first.click(timeout=4000)
                                        await admin.wait_for_timeout(3500)
                                        break
                                    except Exception:
                                        continue
                            except Exception as e:
                                print("  card click failed:", s["card"], e)
                            try:
                                await admin.keyboard.press("Escape")
                                await admin.wait_for_timeout(600)
                            except Exception:
                                pass
                        # finance tab switch
                        if s.get("tab"):
                            try:
                                await admin.get_by_text(s["tab"], exact=False).first.click()
                                await admin.wait_for_timeout(1500)
                            except Exception as e:
                                print("  tab click failed:", s["tab"], e)
                            if s.get("add_btn"):
                                try:
                                    await admin.locator('button:has-text("'+s["add_btn"]+'")').first.click()
                                    await admin.wait_for_timeout(1800)
                                except Exception as e:
                                    print("  add_btn failed:", s["add_btn"], e)
                        # finance detail / approval
                        if s.get("detail"):
                            try:
                                # click first 详情 whose row status is not 已完成
                                await admin.locator('button:has-text("详情")').first.click()
                                await admin.wait_for_timeout(1800)
                            except Exception as e:
                                print("  detail click failed:", e)
                        if s["drill"] and not s.get("card"):
                            await drill(admin)
                    await shot(admin, s["file"])

                elif side == "risk":
                    await risk.goto(s["route"], wait_until="networkidle")
                    await risk.wait_for_timeout(2000)
                    if s.get("menu"):
                        try:
                            await risk.get_by_text(s["menu"], exact=False).first.click()
                            await risk.wait_for_timeout(1800)
                        except Exception as e:
                            print("  risk menu click failed:", s["menu"], e)
                    if s["drill"]:
                        try:
                            await risk.locator('button:has-text("新增"), button:has-text("添加")').first.click()
                            await risk.wait_for_timeout(1500)
                        except Exception as e:
                            print("  risk add click failed:", e)
                    await shot(risk, s["file"])

                elif side == "merchant":
                    await merch.goto(s["route"], wait_until="networkidle")
                    await merch.wait_for_timeout(2000)
                    if s.get("click_text"):
                        try:
                            await merch.get_by_text(s["click_text"], exact=False).first.click()
                            await merch.wait_for_timeout(1800)
                        except Exception as e:
                            print("  merchant click_text failed:", e)
                    if s["drill"]:
                        try:
                            await merch.locator('button:has-text("申请商品入驻"), button:has-text("下一步"), button:has-text("提交")').first.click()
                            await merch.wait_for_timeout(1500)
                        except Exception as e:
                            print("  merchant click failed:", e)
                    await shot(merch, s["file"])

                elif side == "customer":
                    await cust.goto(s["route"], wait_until="networkidle")
                    await cust.wait_for_timeout(2000)
                    if s["drill"]:
                        try:
                            await cust.locator('a[href*="product"], .product-item, [class*="product"]').first.click()
                            await cust.wait_for_timeout(2000)
                        except Exception as e:
                            print("  customer click failed:", e)
                    await shot(cust, s["file"])

            except Exception as e:
                print(f"[ERROR] shot {s['id']} {s['file']}: {e}")
                # still try a screenshot to not break the sequence
                try:
                    pg = {"admin":admin,"risk":risk,"merchant":merch,"customer":cust}.get(side)
                    if pg:
                        await pg.screenshot(path=os.path.join(SHOTS_DIR, s["file"]), full_page=False)
                except Exception:
                    pass

        await browser.close()
    print("DONE. shots in", SHOTS_DIR)

asyncio.run(main())
