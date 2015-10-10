#!/usr/bin/env python
# -*- coding: utf-8 -*-

import scrapy
from routeCrawler.items import RouteCrawlerItem

class RouteCrawler(scrapy.Spider):
    name = "bikemap"
    allowed_domains = "http://www.bikemap.net"
    start_urls = ["http://www.bikemap.net/en/search/?q=zurich"]

    def parse(self, response):
	for result in response.xpath('//li[@class="search-result route"]'):
	    item = RouteCrawlerItem() 

	    item['name'] = result.xpath('div/h2[@class="search-result-header"]/a/text()').extract()[0]

	    item['distance'] = result.xpath('div/div/div[@class="search-result__info"]/dl/dd/text()').re('[0-9]+ km')
	    if not item['distance']:
		# Set distance as -1 if it is not provided
		item['distance'] = -1 
	    else:
		item['distance'] = int(item['distance'][0].strip(' km'))

	    item['elevation'] = result.xpath('div/div/div[@class="search-result__info"]/dl/dd/text()').re('[0-9]+ hm')
	    if not item['elevation']:
		# Set elevation as -1 if it is not provided
		item['elevation'] = -1
	    else:
		item['elevation'] = int(item['elevation'][0].strip(' hm'))

	    item['kml_url'] = result.xpath('@data-kml-url').extract()[0]
	    #TODO: download KML files

	    item['snapshot_url'] = result.xpath('div/div/div[@class="search-result__image"]/a/img/@src').extract()[0]
	    item['snapshot_url'] = unicode(self.allowed_domains, 'utf-8') + item['snapshot_url']

	    print item

	    #TODO: store item into database
