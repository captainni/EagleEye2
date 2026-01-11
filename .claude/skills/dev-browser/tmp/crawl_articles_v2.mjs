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
    await page.waitForTimeout(3000);
    
    const content = await page.evaluate(() => {
      // Get title
      const titleEl = document.querySelector('h1') || document.querySelector('.title');
      const title = titleEl ? titleEl.textContent.trim() : '';
      
      // Get author and time
      let author = '';
      let time = '';
      
      const metaText = document.querySelector('.titbar')?.textContent || 
                      document.querySelector('.article-meta')?.textContent || '';
      
      // Extract author (after "责任编辑")
      const authorMatch = metaText.match(/责任编辑[：:]\s*([^\s]+)/);
      if (authorMatch) {
        author = '责任编辑：' + authorMatch[1];
      }
      
      // Extract time
      const timeMatch = metaText.match(/\d{4}-\d{2}-\d{2}\s+\d{2}:\d{2}:\d{2}/);
      if (timeMatch) {
        time = timeMatch[0];
      }
      
      // Get article body - try more selectors
      const bodySelectors = [
        '#articleBody',
        '.article-content p',
        '.contenttext p',
        '.article p',
        'article p',
        '.text p'
      ];
      
      let body = '';
      for (const selector of bodySelectors) {
        const elements = document.querySelectorAll(selector);
        if (elements.length > 0) {
          const paragraphs = [];
          elements.forEach(el => {
            const text = el.textContent.trim();
            if (text.length > 10) {
              paragraphs.push(text);
            }
          });
          if (paragraphs.length > 0) {
            body = paragraphs.join('\n\n');
            break;
          }
        }
      }
      
      // Fallback: get all text from main content area
      if (!body) {
        const mainContent = document.querySelector('#articleBody') || 
                           document.querySelector('.article-content') ||
                           document.querySelector('article');
        if (mainContent) {
          body = mainContent.textContent.trim();
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
