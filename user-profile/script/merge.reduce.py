#coding=utf-8
#author: hehuihui@meituan.com
#date: 2015-08-20
#brief: 酒店用户画像——价格、房型、品牌偏好

import sys
import math
import json

def Usage():
    if len(sys.argv) != 2:
        print >> sys.stderr, "Usage: python %s <deal_info>" % sys.argv[0]
        sys.exit(-1)

# deal信息
def load_deal_info(file):
    deal_info_dict = {}
    with open(file) as fp:
        for line in fp:
            fields = line.strip().split('\t')
            if len(fields) < 10:
                continue
            if line.startswith("id"):
                continue

            (id, deal_id, poi_id) = fields[0:3]
            (roomtype, hourroom, hoteltype, brand_id, brand_name) = fields[6:11]

            # 房间类型
            if hourroom not in ["DR", "HR"]:
                hourroom = "DR"

            # 品牌
            pos = brand_name.find("（")
            if pos > 0:
                brand_name = brand_name[0:pos]

            deal_info_dict[deal_id] = (roomtype, hourroom, hoteltype, brand_name)
    return deal_info_dict

def get_price_result(price_list):
    if len(price_list) == 0:
        return ""
    price_total = 0
    room_total  = 0
    for (amount, room) in price_list:
        price_total += amount
        room_total  += room
    
    price_avg = round(price_total / room_total, 2)
    price_mae = 0    # 价格均方差
    price_high = 0
    price_low = 10000

    for (amount, room) in price_list:
        price = amount / room
        price_mae += math.pow((price - price_avg), 2) * room
        if price_high < price:
            price_high = price
        if price_low  > price:
            price_low  = price
    price_mae = math.sqrt(price_mae / room_total)
    price_mae = round(price_mae, 2)

    price_dict = {"avg": price_avg, "mae": price_mae, "high": price_high, "low": price_low, "roomnight": room_total}
    return json.dumps(price_dict)
    

def main():
    DEAL_ROOM_FILE = sys.argv[1]
    deal_info_dict = load_deal_info(DEAL_ROOM_FILE)

    last_uid = None
    roomtype_dict = {}        # 房间类型：单人、双人、大床
    hourroom_dict = {}        # 入住时间：全日、钟点
    hoteltype_dict = {}        # 酒店类型：经济、豪华、主题
    brand_dict = {}            # 品牌偏好
    pay_time_list = []        # 最近购买时间
    price_list = []         # 消费价格列表

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
            price_json = get_price_result(price_list)

            roomtype_json = json.dumps(roomtype_dict)
            hourroom_json = json.dumps(hourroom_dict)
            hoteltype_json = json.dumps(hoteltype_dict)

            pay_time_list.sort(reverse = True)
            last_pay_time = pay_time_list[0]
            brand_json = json.dumps(brand_dict, ensure_ascii = False)

            print("%s\t%s\t%s\t%s\t%s\t%s\t%s" % (last_uid, price_json, roomtype_json, hourroom_json, hoteltype_json, last_pay_time, brand_json))

            last_uid = uid
            roomtype_dict.clear()
            hourroom_dict.clear()
            hoteltype_dict.clear()
            brand_dict.clear()
            del price_list[:]
            del pay_time_list[:]

        # 取各个deal的信息
        try:
            pay_amount = float(pay_amount)
            pay_roomnight = int(pay_roomnight)
        except ValueError as e:
            print >> sys.stderr, e
            continue

        if pay_roomnight <= 0:
            continue
        # 时间间隔
        pay_time_list.append(pay_time)
        # 价格
        price_list.append([pay_amount, pay_roomnight])

        # 解析数据
        if goods_id not in deal_info_dict:
            continue
        (roomtype, hourroom, hoteltype, brand_name) = deal_info_dict[goods_id]

        # 房间类型
        for r in roomtype.split(","):
            roomtype_dict.setdefault(r, 0)
            roomtype_dict[r] += pay_roomnight

        # 入住时间
        hourroom_dict.setdefault(hourroom, 0)
        hourroom_dict[hourroom] += pay_roomnight

        # 酒店类型
        for h in hoteltype.split(","):
            hoteltype_dict.setdefault(h, 0)
            hoteltype_dict[h] += pay_roomnight

        # 品牌
        if brand_name != "":
            brand_dict.setdefault(brand_name, 0)
            brand_dict[brand_name] += pay_roomnight

if __name__ == '__main__':
    Usage()
    main()

