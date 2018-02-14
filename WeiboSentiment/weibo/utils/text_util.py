# -*- coding: utf-8 -*-
import re
import types


def remove_multi_space(text):
  text = re.sub(r"\xa0", " ", text)
  text = re.sub(u'\u200b', '', text)
  return re.sub(r"\s{2,}", " ", text)


def remove_html_space(text):
  return re.sub('\xa0', "", text)


def remove_newline_character(text):
  return re.sub(r"\n", " ", text)

def replace_with_newlines(element):
  text = ''
  for elem in element.recursiveChildGenerator():
    if isinstance(elem, types.StringTypes):
      text += elem.strip()
    elif elem.name == 'br':
      text += '\n'
  return text
