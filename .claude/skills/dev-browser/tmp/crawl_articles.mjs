import { connect, waitForPageLoad } from "@/client.js";

const articles = [
  {
    "url": "https://finance.jrj.com.cn/2026/01/10115255002570.shtml",
    "title": "黄奇帆：建议以银行、社保、保险、外汇资金设立股权引导基金 规模可达四、五十万亿元23小时前"
  },
  {
    "url": "https://bank.jrj.com.cn/2026/01/09185354994712.shtml",
    "title": "十五五期间，中小银行减量后如何提质？01-09 18:53"
  },
  {
    "url": "https://bank.jrj.com.cn/2026/01/09184154994395.shtml",
    "title": "2025年深圳人民银行、深圳外汇局十件大事，发布！01-09 18:41"
  }
];

async function main() {
  const client = await connect();
  const page = await client.page("article_crawler", { viewport: { width: 1920, height: 1080 } });
  
  const results = [];
  
  for (let i = 0; i < articles.length; i++) {
    const article = articles[i];
    console.log("\n=== Crawling article " + (i + 1) + ": " + article.title + " ===");
    
    await page.goto(article.url);
    await waitForPageLoad(page);
    await page.waitForTimeout(2000);
    
    const content = await page.evaluate(() => {
      // Try different selectors for article content
      const titleEl = document.querySelector('h1') || 
                   document.querySelector('.title') ||
                   document.querySelector('.article-title');
      const title = titleEl ? titleEl.textContent.trim() : '';
      
      // Try to find author
      const authorEl = document.querySelector('.author') ||
                    document.querySelector('.source') ||
                    document.querySelector('.editor');
      const author = authorEl ? authorEl.textContent.trim() : '';
      
      // Try to find publish time
      const timeEl = document.querySelector('.time') ||
                   document.querySelector('.pub-time') ||
                   document.querySelector('.date');
      const time = timeEl ? timeEl.textContent.trim() : '';
      
      // Try to find article body
      const bodySelectors = [
        '.article-content',
        '.content',
        '.article-body',
        '#articleBody',
        '.text-content',
        'article'
      ];
      
      let body = '';
      for (const selector of bodySelectors) {
        const el = document.querySelector(selector);
        if (el && el.textContent.trim().length > 100) {
          body = el.textContent.trim();
          break;
        }
      }
      
      return { title, author, time, body };
    });
    
    results.push({
      index: i + 1,
      url: article.url,
      ...content
    });
    
    console.log("Title: " + content.title);
    console.log("Author: " + content.author);
    console.log("Time: " + content.time);
    console.log("Body length: " + content.body.length + " characters");
  }
  
  console.log("\n=== ALL ARTICLES ===");
  console.log(JSON.stringify(results, null, 2));
  
  await client.disconnect();
}

main();
