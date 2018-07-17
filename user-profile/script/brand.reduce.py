#coding=utf-8
#author: hehuihui@meituan.com
#date: 2015-08-20
#brief: 酒店用户画像——品牌

import sys
import json

def Usage():
    if len(sys.argv) != 2:
        print >> sys.stderr, "Usage: python %s <deal_room>" % sys.argv[0]
        sys.exit(-1)

# deal的品牌信息
def load_deal_brand(file):
    deal_brand_dict = {}
    with open(file) as fp:
        for line in fp:
            fields = line.strip().split('\t')
            if len(fields) < 10:
                continue
            if line.startswith("id"):
                continue

            deal_id = fields[1]
            brand_name = fields[10]

            pos = brand_name.find("（")
            if pos > 0:
                brand_name = brand_name[0:pos]

            deal_brand_dict[deal_id] = brand_name
    return deal_brand_dict

def main():
    DEAL_BRAND_FILE = sys.argv[1]
    deal_brand_dict = load_deal_brand(DEAL_BRAND_FILE)

    last_uid = None
    pay_time_list = []
    brand_dict = {}

    for line in sys.stdin:
        fields = line.strip().split('\t')
        if len(fields) != 9:
            continue
        (uid, biz_type, pay_time, order_id, user_id, poi_id, \
         goods_id, pay_amount, pay_roomnight) = fields

        # first user
        if last_uid is None:
            last_uid = uid

        # next user
        if last_uid != uid:
            pay_time_list.sort(reverse = True)
            last_pay_time = pay_time_list[0]
            brand_json = json.dumps(brand_dict, ensure_ascii = False)
            print("%s\t%s\t%s" % (last_uid, last_pay_time, brand_json))

            last_uid = uid
            del pay_time_list[:]
            brand_dict.clear()

        # process 
        pay_roomnight = int(pay_roomnight)
        
        # 品牌
        brand_name = deal_brand_dict.get(goods_id, "")
        if brand_name != "":
            brand_dict.setdefault(brand_name, 0)
            brand_dict[brand_name] += pay_roomnight
        # 时间间隔
        pay_time_list.append(pay_time)


if __name__ == '__main__':
    Usage()
    main()


