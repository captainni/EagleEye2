import { connect, waitForPageLoad } from "@/client.js";

async function main() {
  const client = await connect();
  const page = await client.page("bank_jrj", { viewport: { width: 1920, height: 1080 } });

  await page.goto("https://bank.jrj.com.cn/");
  await waitForPageLoad(page);

  // Wait for dynamic content to load
  await page.waitForTimeout(3000);

  // Extract article links and titles
  const articles = await page.evaluate(() => {
    const links = [];
    const linkElements = document.querySelectorAll('a[href]');
    
    linkElements.forEach((el) => {
      const href = el.getAttribute('href');
      const title = el.textContent?.trim();
      
      if (href && title && href.includes('/202') && (href.includes('.shtml') || href.includes('.html'))) {
        // Convert relative URLs to absolute
        const fullUrl = href.startsWith('http') ? href : `https://bank.jrj.com.cn${href}`;
        if (!links.find(l => l.url === fullUrl)) {
          links.push({ url: fullUrl, title });
        }
      }
    });
    
    return links.slice(0, 3); // Only get first 3 articles
  });

  console.log(JSON.stringify(articles, null, 2));

  await client.disconnect();
}

main();
