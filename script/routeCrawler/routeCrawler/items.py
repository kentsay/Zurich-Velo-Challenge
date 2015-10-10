# -*- coding: utf-8 -*-

# Define here the models for your scraped items
#
# See documentation in:
# http://doc.scrapy.org/en/latest/topics/items.html

import scrapy


class RoutecrawlerItem(scrapy.Item):
    # define the fields for your item here like:
    name = scrapy.Field()
    distance = scrapy.Field()
    elevation = scrapy.Field()
    snapshot_url = scrapy.Field()
    kml_url = scrapy.Field()
