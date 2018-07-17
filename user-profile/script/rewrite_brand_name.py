#coding=utf-8
#author: hehuihui@meituan.com
#date: 2015-08-20
#brief: 酒店品牌名规范化

import sys

def main():
    for line in sys.stdin:
        fields = line.strip().split('\t')
        if len(fields) < 10:
            continue

        brand_name = fields[10]
        pos = brand_name.find("（")
        if pos >= 0:
            print brand_name[0:pos]
        else:
            print brand_name

if __name__ == "__main__":
    main()
