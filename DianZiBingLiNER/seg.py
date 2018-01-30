# -*- encoding: utf-8 -*-
import json
import requests


TAG_URL = 'http://api.bosonnlp.com/tag/analysis'
# 如果某个选项采用默认设置，可以在TAG_URL中省略，完整的TAG_URL如下：
# 'http://api.bosonnlp.com/tag/analysis?space_mode=0&oov_level=3&t2s=0&special_char_conv=0'
# 修改space_mode选项为1
# TAG_URL = \
#   'http://api.bosonnlp.com/tag/analysis?space_mode=1'
# 修改oov_level选项为1
# TAG_URL = \
#    'http://api.bosonnlp.com/tag/analysis?oov_level=1'
# 修改t2s选项为1
# TAG_URL= \
#     'http://api.bosonnlp.com/tag/analysis?t2s=1'
# 修改special_char_conv选项为1
# TAG_URL= \
# 'http://api.bosonnlp.com/tag/analysis?special_char_conv=1'

s = []
file = 'bingli2.txt'
f = open(file, 'r')
for line in f:
    s.append(line)
f.close()

data = json.dumps(s)
# 这里请去bosonnlp注册账号，填入你自己的Token
headers = {'X-Token': '用你自己注册的Token'}
resp = requests.post(TAG_URL, headers=headers, data=data.encode('utf-8'))

result = []

for d in resp.json():
    rs = ' '.join(['%s/%s' % it for it in zip(d['word'], d['tag'])])
    result.append(rs)

ffile = file + '.txt'
fr = open(ffile, 'a')
for r in result:
  fr.write(r)
  fr.write("\n")
fr.close()