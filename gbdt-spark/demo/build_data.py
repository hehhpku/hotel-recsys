import random
import math


for i in range(0, 10000):
    x = random.randint(-10, 10)
    y = random.randint(-10, 10)
    z = random.randint(-10, 10)
    w = x + 2*y -z

    v = 1.0/(1+math.exp(-w))

    # weight label idx:value
    print '1 %s 0:%s 1:%s 2:%s' % (v, x, y, z,)
