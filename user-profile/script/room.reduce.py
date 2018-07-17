#coding=utf-8
#author: hehuihui@meituan.com
#date: 2015-08-20
#brief: 酒店用户画像——房型

import sys
import json

def Usage():
    if len(sys.argv) != 2:
        print >> sys.stderr, "Usage: python %s <deal_room>" % sys.argv[0]
        sys.exit(-1)

# deal的房型信息
def load_deal_room(file):
    deal_room_dict = {}
    with open(file) as fp:
        for line in fp:
            fields = line.strip().split('\t')
            if len(fields) < 10:
                continue
            if line.startswith("id"):
                continue

            (id, deal_id, poi_id) = fields[0:3]
            (roomtype, hourroom, hoteltype) = fields[6:9]

            if hourroom not in ["DR", "HR"]:
                hourroom = "DR"

            deal_room_dict[deal_id] = (roomtype, hourroom, hoteltype)
    return deal_room_dict

def main():
    DEAL_ROOM_FILE = sys.argv[1]
    deal_room_dict = load_deal_room(DEAL_ROOM_FILE)

    last_uid = None
    roomtype_dict = {}        #房间类型：单人、双人、大床
    hourroom_dict = {}        #入住时间：全日、钟点
    hoteltype_dict = {}        #酒店类型：经济、豪华、主题

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
            roomtype_json = json.dumps(roomtype_dict)
            hourroom_json = json.dumps(hourroom_dict)
            hoteltype_json = json.dumps(hoteltype_dict)
            print("%s\t%s\t%s\t%s" % (last_uid, roomtype_json, hourroom_json, hoteltype_json))

            last_uid = uid
            roomtype_dict.clear()
            hourroom_dict.clear()
            hoteltype_dict.clear()

        # 取各个deal的房型信息
        pay_roomnight = int(pay_roomnight)
        if goods_id not in deal_room_dict:
            continue
        (roomtype, hourroom, hoteltype) = deal_room_dict[goods_id]

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

if __name__ == '__main__':
    Usage()
    main()

