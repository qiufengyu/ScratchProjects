from util import utils
from data import get_data
from analysis import analysis


if __name__ == '__main__':
  # 获取股票代码和名称的映射，运行一次即可
  utils.get_stock_list()
  # 需要手动添加两项：
  # 上证指数,sh000001
  # 深证成指,399001
  get_data.get_hist_data_by_code('600611')
  analysis.analysis_stock('600611')
  analysis.analysis_stock_news('600611')


