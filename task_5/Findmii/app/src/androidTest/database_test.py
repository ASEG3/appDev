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

# Random test sample, finds and generates a random sample of 1000 postcodes.

cursor.execute('''
                SELECT * FROM postcode
                ''')
entire_list_of_postcodes = cursor.fetchall()

postcodes = [entire_list_of_postcodes[i][0] for i in sorted(random.sample(range(len(entire_list_of_postcodes)),1000))]

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
assert 1 in weights and 0 in weights


#generates 1000 random lat and long positions for use in the applications testing framework

random_pointers = sorted(random.sample(range(len(entire_list_of_postcodes)),1000))

sample_lat = [entire_list_of_postcodes[i][2] for i in random_pointers]
sample_lng = [entire_list_of_postcodes[i][3] for i in random_pointers]

f = open("sample.txt", 'w')
for i in range(len(sample_lat)):
    if(sample_lat[i] is not None):
        f.write("sampleLatLngs.put(" +  str(sample_lat[i]) + "," + str(sample_lng[i]) + ");\n")
f.close()

