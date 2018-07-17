#coding=utf-8
#brief: 计算酒店用户画像- 常驻地

import sys

UID_LIST = ['10092634', '593414', '52491818', '14811669', '38161404', '2232768']

def main():
    for line in sys.stdin:
        fields = line.strip().split('\t')
        if len(fields) != 6:
            continue

        user_id = fields[0]
        if user_id in UID_LIST:
            print line.strip()

if __name__ == '__main__':
    main()
