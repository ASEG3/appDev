__author__ = 'matthewweller'

import csv
import os
import MySQLdb
import urllib.request
import json

class databaseParser:
    def cons_csv(self):
        f = open("/Users/matthewweller/PycharmProjects/ALifePart2/new_sort.csv", encoding='utf-8')

        current = ""
        rows = []
        for row in csv.reader(f):
            area = row[3].partition(' ')[0]
            if area == current:
                rows.append(row)
            else:
                with open("/Users/matthewweller/Desktop/areas/" + current + ".csv", 'w', encoding='utf-8') as w:
                    writer = csv.writer(w)
                    writer.writerows(rows)
                    w.close()
                    current = area
                    rows = []
                    rows.append(row)
        if len(rows) != 0:
            with open("/Users/matthewweller/Desktop/areas/" + current + ".csv", 'w', encoding='utf-8') as w:
                writer = csv.writer(w)
                writer.writerows(rows)
                w.close()

        f.close()

    def init_database(self):
        with open("/Users/matthewweller/Desktop/new.txt", encoding='utf-8') as f:
            content = [x.strip('\n') for x in f.readlines()]
        connection = MySQLdb.connect(host="localhost", user="root", passwd="pass")
        # host="52.11.103.82", user="root", passwd="g3mjhmts", db="location"
        cursor = connection.cursor()
        cursor.execute('''
            CREATE DATABASE ase
        ''')
        cursor.execute('''
            USE ase
        ''')
        cursor.execute('''
            DROP TABLE IF EXISTS postcode
        ''')
        cursor.execute('''
            CREATE TABLE postcode(
            postcodeID VARCHAR(255) NOT NULL,
            average_price DOUBLE,
            PRIMARY KEY (postcodeID)
            )
        ''')
        cursor.execute('''
            DROP TABLE IF EXISTS house
        ''')
        cursor.execute('''
            CREATE TABLE house
            (
            houseID VARCHAR(255) NOT NULL,
            PAON VARCHAR(255),
            SAON VARCHAR(255),
            street VARCHAR(255),
            locality VARCHAR(255),
            city VARCHAR(255),
            district VARCHAR(255),
            county VARCHAR(255),
            postcode VARCHAR(255) NOT NULL,
            PRIMARY KEY (houseID),
            CONSTRAINT fk_postcode FOREIGN KEY(postcode) REFERENCES postcode(postcodeID) ON DELETE CASCADE
            )
        ''')
        cursor.execute('''
            CREATE TABLE sale
            (
            saleID VARCHAR(255) NOT NULL,
            price_sold DOUBLE,
            date_sold DATETIME,
            houseID VARCHAR(255) NOT NULL,
            PRIMARY KEY (saleID),
            CONSTRAINT fk_house FOREIGN KEY(houseID) REFERENCES house(houseID) ON DELETE CASCADE
            )
        ''')
        path = "/Users/matthewweller/Desktop/areas/"
        for file in os.listdir(path):
            if ".csv" in file:
                with open(path+file, encoding='utf-8') as f:
                    reader = csv.reader(f)
                    current = ""
                    house_ID_collection = set()
                    for row in reader:
                        tmp = row[3].strip()
                        PAON = row[7].strip()
                        SAON = row[8].strip()
                        street = row[9].strip()
                        locality = row[10].strip()
                        city = row[11].strip()
                        district = row[12].strip()
                        county = row[13].strip()
                        if current != tmp:
                            current = tmp
                            print(current)
                            cursor.execute('''
                                INSERT INTO postcode(postcodeID)
                                VALUES (%s)''', ([current]))
                        if current + row[7] + row[8] not in house_ID_collection:
                            cursor.execute('''
                                INSERT INTO house(houseID, PAON, SAON, street, locality, city, district, county, postcode)
                                VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s)''',(current+PAON+SAON,PAON,SAON,street,
                                                               locality,city,district, county,current))
                            house_ID_collection.add(current + PAON + SAON)
                        cursor.execute('''
                            INSERT INTO sale(saleID, price_sold, date_sold, houseID)
                            VALUES (%s,%s,%s,%s)''',(row[0], row[1], row[2], current + PAON + SAON))
                    f.close()

        # cursor.execute('''
        #
        #     # SELECT h.* FROM postcode p
        #     # JOIN house h ON h.postcode = p.postcodeID
        #     # WHERE postcodeID="BN1 1AD"
        # ''')
        # print(cursor.fetchall())

        connection.commit()
        connection.close()

    def insert_lat_long(self):
            connection = MySQLdb.connect(host="localhost", user="root", passwd="pass", db="ase")
            # host="52.11.103.82", user="root", passwd="g3mjhmts", db="location"
            cursor = connection.cursor()
            cursor.execute('''
                    SELECT postcodeID FROM postcode
                ''')
            results = cursor.fetchall()
            with open("/Users/matthewweller/Downloads/postcode-outcodes.csv", encoding="utf-8") as f:
                reader = csv.reader(f)
                for postcode in results:
                    code = postcode[0].strip()
                    cursor.execute('''
                        SELECT latit, longit FROM postcode WHERE postcodeID = %s
                    ''',[code])
                    tmp = cursor.fetchall()
                    if tmp[0][0] == None or tmp[0][1] == None:
                        out = code.partition(" ")[0]
                        for row in reader:
                            if out == row[1]:
                                print(code)
                                cursor.execute('''
                                    UPDATE postcode
                                    SET longit = %s, latit = %s
                                    WHERE postcodeID = %s
                                ''',(row[3],row[2],code))
                f.close()
            connection.commit()
            connection.close()

    def remove_company(self):
        connection = MySQLdb.connect(host="localhost", user="root", passwd="pass", db="ase")
        # host="52.11.103.82", user="root", passwd="g3mjhmts", db="location"
        cursor = connection.cursor()
        with open("/Users/matthewweller/PycharmProjects/ALifePart2/new_sort.csv", encoding="utf-8") as f:
            reader = csv.reader(f)
            housecount = 0
            current = ""
            addresses_to_remove = []
            for row in reader:
                if current != row[3].strip():
                    if housecount == 0:
                        print(current)
                        addresses_to_remove.append(current)
                    housecount = 0
                    current = row[3].strip()
                if row[4] != "O":
                    housecount += 1
            if housecount == 0:
                print(current)
                addresses_to_remove.append(current)
            f.close()
        for company in addresses_to_remove:
            print(company)
            cursor.execute('''
                DELETE FROM postcode WHERE postcodeID = %s
            ''', [company])
        connection.commit()
        connection.close()

    def update_average(self):
        connection = MySQLdb.connect(host="localhost", user="root", passwd="pass", db="ase")
        cursor = connection.cursor()
        cursor.execute('''
            UPDATE postcode p
            JOIN (  SELECT postcode, AVG(price_sold) as average
                    FROM house
                    GROUP BY postcode
                ) h ON p.postcodeID = h.postcode
            SET p.average_price = h.average
        ''')
        connection.commit()
        connection.close()

if __name__ == '__main__':
    foo = databaseParser()
    foo.update_average()




