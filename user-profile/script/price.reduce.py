#coding=utf-8
#brief: 计算酒店用户画像-消费水平

import sys
import json
import math

def main():
    last_uid = None
    price_list = []        # 消费价格列表
    price_total = 0        # 消费总价
    rn_total = 0        # 间夜总数

    for line in sys.stdin:
        fields = line.strip().split('\t')
        if len(fields) != 9:
            continue
        (uid, biz_type, pay_time, order_id, user_id, poi_id, \
         goods_id, pay_amount, pay_roomnight) = fields

        try:
            pay_amount = float(pay_amount)
            pay_roomnight = int(pay_roomnight)
        except ValueError as e:
            print >> sys.stderr, e
            continue
        if pay_roomnight <= 0:
            continue

        # first user
        if last_uid is None:
            last_uid = uid

        # next user
        if last_uid != uid:
            if rn_total <= 0:
                rn_total = 1
            price_mae = 0        # 价格均方差
            price_high = 0        # 最高价
            price_low  = 10000    # 最低价
            price_avg = price_total / rn_total

            for price in price_list:
                price_mae += math.pow((price - price_avg), 2)
                if price_high < price:
                    price_high = price
                if price_low > price:
                    price_low  = price
            price_mae = math.sqrt(price_mae / rn_total)

            print("%s\t%d\t%.2f\t%.2f\t%.2f\t%.2f" % (last_uid, rn_total, price_avg, price_mae, price_high, price_low))
            last_uid = uid
            del price_list[:]
            price_total = 0
            rn_total = 0

        price_total += pay_amount
        rn_total += pay_roomnight
        for i in xrange(0, pay_roomnight):
            mean_cost = pay_amount / pay_roomnight
            price_list.append(mean_cost)

if __name__ == '__main__':
    main()

