#!/usr/bin/env python
# -*- coding: utf-8 -*-

import sqlite3
import scrapy
from scrapy import signals
from scrapy.xlib.pydispatch import dispatcher
from routeCrawler.items import RouteCrawlerItem

class RouteCrawler(scrapy.Spider):
    name = "bikemap"

    def __init__(self, category='', domain=''):
	# Connect to spider_closed signal
	dispatcher.connect(self.spider_closed, signals.spider_closed)

	# Connect to SQLite database
	self.conn = sqlite3.connect('routes.db')
	self.c = self.conn.cursor()
	self.c.execute('''CREATE TABLE if not exists routes (distance, snapshot_url, elevation, name, kml_url)''')

	# Standard Scrapy settings
	self.allowed_domains = "http://www.bikemap.net"
	url_prefix = "http://www.bikemap.net/en/regional/Switzerland/Zurich/Zurich/?page="
	self.start_urls = []
	# Note: the number of pages is hard-coded
	for i in range(1, 82):
	    self.start_urls.append(url_prefix + str(i))

	print "Connected to DB and initialized the crawler"

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

	    #print item

	    # Store the item into database
	    self.c.execute('INSERT INTO routes VALUES (?,?,?,?,?)', item.values())
	    self.conn.commit()

    def spider_closed(self, spider):
	self.conn.close()
	print "Close DB connection successfully."

