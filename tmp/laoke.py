#encoding=utf-8
'''
Created on 2015-2-11

@author: yejun
'''
import sys
reload(sys)
sys.setdefaultencoding('utf-8')  # @UndefinedVariable
from lxml import etree
from StringIO import StringIO
import pycurl
import DBYPage

def download(url):
    try:
        curl = pycurl.Curl()
        head = ['Accept:*/*',
#                 'Accept-Encoding:gzip,deflate,sdch',
#                 'Accept-Language:zh-CN,zh;q=0.8',
#                 'Content-encoding: gzip',
                'User-Agent:Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.97 Safari/537.11']
        buf =  StringIO()
        curl.setopt(pycurl.WRITEFUNCTION, buf.write)
        curl.setopt(pycurl.URL, url.decode("utf-8").encode("utf-8"))
        curl.setopt(pycurl.HTTPHEADER, head)
        curl.setopt(pycurl.FOLLOWLOCATION, 5)
        curl.perform()
        the_page = buf.getvalue()
        try:
            the_page = the_page.decode("utf-8")
        except:
            try:
                the_page = the_page.decode("gbk")
            except Exception, e:
                print e
                the_page = None
        buf.close()
        return the_page
    except Exception, e:
        print e
        return None
            

def seed_city_page():
    url = "http://www.laoke.com/default.aspx"
    array = []
    body = download(url)
    response = etree.HTML(body)
    for sel in  response.xpath("//div[@class='d_content']/dl/dd/a"):
        herf = sel.xpath("@href")
        city = sel.xpath("text()")
        info = herf[0].strip() + "/huangye/index.aspx" + "\t" + city[0].strip()
        print info
        array.append(info)
        
    with open("data/level1_city_url_name.info", "w") as out:
        out.write("\n".join(array))
        
def seed_city_lev2():
    out = open("data/level2_city_url_name.info","w")
    for seed in open("data/level1_city_url_name.info"):
        if len(seed.strip().split("\t")) < 2:continue
        url = seed.strip().split("\t")[0]
        turl = url.replace("/huangye/index.aspx","")
        city = seed.strip().split("\t")[1]
        body = download(url)
        response = etree.HTML(body)
        tlist = []
        for sel in  response.xpath("//li[@class='l_item_b']/a"):
            herf = sel.xpath("@href")
            cat = sel.xpath("text()")
            info = turl + herf[0].strip() + "\t" + cat[0].strip() + "\t" + city
            tlist.append(info)
        print len(tlist), url
        out.write("\n".join(tlist) + "\n")
def seed_city_lev2_page():
    out = open("data/level3_city_url_name.info","w")
    for seed in open("data/level2_city_url_name.info"):
        if len(seed.strip().split("\t")) < 3:continue
        url = seed.strip().split("\t")[0]
        body = download(url)
        response = etree.HTML(body)
        num = response.xpath("//div[@class='fybar']/span[@class='text_red'][1]/text()");
        if num:
            num = num[0].strip()
        else:
            num = "0"
        print seed.strip() +"\t" + num
        out.write(seed.strip() + "\t" + num + "\n")

def get_page_info():
#     http://xian.laoke.com/huangye/bianminfuwu
#     http://xian.laoke.com/huangye/bianminfuwu/pn2
    links = []
    out = open("data/level4_city_url_name.info", "w")
    for seed in open("data/level3_city_url_name.info"):
        if len(seed.strip().split("\t")) < 4:continue
        url = seed.strip().split("\t")[0]
        cat = seed.strip().split("\t")[1]
#         city = seed.strip().split("\t")[2]
        num = seed.strip().split("\t")[3]
        if num != "0":
            import string
            page =  string.atoi(num) / 15 if  string.atoi(num) % 15 == 0 else  string.atoi(num) / 15 + 1;
            for index in range(1,page + 1):
                link = "%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s"%(url+"/pn%d"%index, cat, "", "0", "0", "laoke.com", "laoke.com", "0", "0")
                print ",".join(link.split("\n"))
                out.write("\t".join(link.split("\n")) + "\n")
                links.append(link.split("\n"))
    print len(links)
    DBYPage.insert_page_links(links);


        
def getInfo():
    tmap = {}
    for line in open("data/level3_city_url_name.info"):
        if len(line.strip().split("\t")) < 4:continue
        url = line.strip().split("\t")[0].split("/huangye")[0]
        city = line.strip().split("\t")[2]
        tmap[url] = city
        
    task_queue = Queue.Queue()
    worker_size = 5;
    
    project_name = "laoke.com"
    page_link_objects = DBYPage.select_page_link_objects(project_name)
    
    if page_link_objects and len(page_link_objects) > 0:
        for link_obj in page_link_objects:
            task_queue.put(link_obj)
    Scheduler(task_queue, worker_size, tmap).start()
    
    
import threading
import Queue

class Worker (threading.Thread):
    def __init__(self, taskQueue, tmap):
        threading.Thread.__init__(self)
        self.taskQueue = taskQueue #thread safe
        self.runnable = True
        self.tmap = tmap
        
    def run(self):
        import time
        while self.runnable:
            if not self.taskQueue.empty():
                task = self.taskQueue.get()
                self.porcess(task)
            else:
                self.runnable = False
        time.sleep(0.3)
        
    def porcess(self, task):
        currentThread = threading.currentThread().getName()
        STATUS = 1
        page_link = task;
        if not page_link: print "page link is null!" ; STATUS = -1; return
        url = page_link.url
        baseurl = url.split("/huangye")[0]
        content = []
        
        body = download(url)
        if not body: print "download error: %s"%url; STATUS = 0; return
        response = etree.HTML(body)
        for sel in  response.xpath("//div[@class='list_item']/ul"):
            company = sel.xpath("li[@class='biaoti']/a/b/text()")
            website = sel.xpath("li[@class='biaoti']/a/@href")
            location = sel.xpath("li[@style='line-height:18px;']/text()")
            
            company = company[0].strip() if company else ""
            website = baseurl + website[0].strip() if website else ""
            xlocation = location[0].strip().replace("地　址：","").strip() if location else ""
            tel = location[1].strip().replace("电　话：","").strip() if location else ""
            city = self.tmap.get(baseurl)
            if tel != "":
                page_content = DBYPage.page_content(url, "", xlocation, "laoke.com", company, tel, website, city)
                info = page_content.show()
                if info: content.append(info.split("\n"))
        try:
            print "[%s] - [%d] -- [%s]"%(currentThread, len(content), url)
        except:
            print "show ---"
        if STATUS == 1:
            DBYPage.update_page_links_isAsk(page_link.url)
            DBYPage.insert_page_contents(content)

class Scheduler():
    def __init__(self, task_queue, worker_size, tmap):
        self.task_queue = task_queue
        self.worker_size = worker_size
        self.workers = []
        self.tmap = tmap
            
    def start(self):
        self.startWorkers();
        self.exitWorkers();
    
    def startWorkers(self):
        for _ in range(self.worker_size):
            worker = Worker(self.task_queue, self.tmap)
            worker.start()
            self.workers.append(worker)
            
    def exitWorkers(self):
        if self.workers:
            for worker in self.workers:worker.join();        

if __name__ == "__main__":
#     seed_city_page()
#     seed_city_lev2()
#     seed_city_lev2_page()
#     get_page_info()
    getInfo()
