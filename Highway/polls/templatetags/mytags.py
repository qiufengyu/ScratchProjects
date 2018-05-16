from django import template
from django.utils.http import urlencode

register = template.Library()

@register.simple_tag(takes_context=True, name='url_replace')
def url_replace(context, **kwargs):
  query = context['request'].GET.dict()
  query.update(kwargs)
  return urlencode(query)
