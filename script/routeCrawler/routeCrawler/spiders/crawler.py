# -*- coding: utf-8 -*-

import scrapy
from routeCrawler.items import RoutecrawlerItem

class routeCrawler(scrapy.Spider):
    name = "bikemap"
    allowed_domains = ["bikemap.net"]
    start_urls = ["http://www.bikemap.net/en/search/?q=zurich"]

    def parse(self, response):
	for result in response.xpath(u'//li[@class="search-result route"]'.encode('utf-8')):
	    item = RoutecrawlerItem() 

	    item['name'] = result.xpath(u'div/h2[@class="search-result-header"]/a/text()'.encode('utf-8')).extract()

	    item['distance'] = result.xpath(u'div/div/div[@class="search-result__info"]/dl/dd/text()'.encode('utf-8')).re('[0-9]+ km');
	    if not item['distance']:
		item['distance'] = -1 
	    else:
		item['distance'] = int(item['distance'][0].strip(' km'));

	    item['elevation'] = result.xpath(u'div/div/div[@class="search-result__info"]/dl/dd/text()'.encode('utf-8')).re('[0-9]+ hm');
	    if not item['elevation']:
		item['elevation'] = -1;
	    else:
		item['elevation'] = int(item['elevation'][0].strip(' hm'));

	    item['kml_url'] = result.xpath('@data-kml-url').extract()
	    item['snapshot_url'] = result.xpath(u'div/div/div[@class="search-result__image"]/a/img/@src'.encode('utf-8')).extract()

	    #print item['name']
	    #print item['distance']
	    #print item['elevation']
	    #print item['kml_url']
	    print item
