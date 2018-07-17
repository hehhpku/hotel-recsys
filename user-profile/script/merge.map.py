#coding=utf-8
#brief: 计算酒店用户画像-消费水平

import sys

def main():
    for line in sys.stdin:
        line = line.strip()
        fields = line.split('\t')
        if len(fields) < 4:
            continue
        user_id = fields[3]
        print("%s\t%s" % (user_id, line))

if __name__ == '__main__':
    main()

