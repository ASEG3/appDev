__author__ = 'matthewweller'

import MySQLdb
import random
import numpy
import csv

average_prices = []
connection = MySQLdb.connect(host="localhost", user="root", passwd="pass")
cursor = connection.cursor()
cursor.execute('''
    USE ase
''')
#test sample, includes a case where a postcode contains only one house.
postcodes = ["AL6 0BX", "DL14 8TN", "IG8 0DL", "IP31 1BD", "NN13 6PX", "OX3 0DS", "GL17 0HA", "CV23 9BD", "BS6 7EQ", "SO45 3GH",
             "TN38 0AL","W11 3QT","YO8 9TP"]
for postcode in postcodes:
    # ensures the correct postcode with its information is displayed
    cursor.execute('''
                        SELECT * FROM postcode WHERE postcodeID = %s
                    ''',[postcode])
    postcode_details = cursor.fetchall()
    print(postcode_details)
    # ensures the correct houses are mapped the their corresponding postcode
    cursor.execute('''
                        SELECT p.postcodeID, h.*
                        FROM postcode p
                        INNER JOIN house h ON p.postcodeID = h.postcode
                        WHERE postcodeID = %s
                    ''',[postcode])
    house_list = cursor.fetchall()
    print(house_list)
    # ensures that for a given postcode, the prices for each house are outputted correctly
    price_list = [house_list[x][10] for x in range(len(house_list))]
    price_list.sort()
    print(price_list)
    # ensures that the average price for a postcode matches the average price of each house belonging to a postcode
    print(postcode_details[0][1] == numpy.average(price_list))
    average_prices.append(numpy.average(price_list))
# ensures that the weighted algorithm works correctly
weights = [(average_prices[x]-min(average_prices))/(max(average_prices) - min(average_prices)) for x in range(len(average_prices))]
print(weights)
print(1 in weights and 0 in weights)
# CLAUSE FOR WHEN THERE IS ONLY ONE POSTCODE IN AREA, POTENTIAL DIVISION BY ZERO, CHANGE SERVLET CODE TO COMPENSATE!!




