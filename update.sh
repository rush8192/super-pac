#!/bin/sh

IF_D="$1"

if [[ "$IF_D" = "-d" ]]
 then
        echo "downloading contribs..."
        curl -L -o 'ind.zip' 'ftp://ftp.fec.gov/FEC/2016/indiv16.zip'
        echo "done."

        echo "downloading PACs......"
        curl -L -o 'pac.zip' 'ftp://ftp.fec.gov/FEC/2016/cm16.zip'
        echo "done."

        echo "unzipping........."
        rm itcont.txt
        rm cm.txt
        unzip \*.zip
        rm -f *.zip
        echo "done."

        echo "importing data into SqlLite........."
        sqlite3 campaignDB < importData.sql
        echo "done."
fi