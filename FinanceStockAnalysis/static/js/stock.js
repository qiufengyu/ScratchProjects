/*
* 通过股票数据绘制股价趋势图
* */

$("#btn-submit").on('click', function () {
        var endpoint = "/api/data";
        var stock_id = $('#stock_id').val();
        $("p.error-message").html('');

        $.ajax({
            method: "GET",
            url: endpoint,
            data: {stockid: stock_id},
            success: function (data) {
                if (data.error_message) {
                    $("p.error-message").html(data.error_message);
                }
                else {
                    // 给出预测结果
                    if (data.flag > 0) {
                        $("p#open-pred").html('预测开盘价：<span class="red-text text-accent-2">' + data.y_open +'</span>');
                        $("p#close-pred").html('预测收盘价：<span class="red-text text-accent-2">' + data.y_close +'</span>');
                        $("p#delta-pred").html('预测涨跌：<span class="red-text text-accent-2"> + ' + data.delta + ' (+' + data.delta_percent+'%)</span>');
                        $("p#confidence-pred").html('涨跌置信度：<span class="red-text text-accent-2">' + data.senti +'</span>');
                    }
                    else {
                        $("p#open-pred").html('预测开盘价：<span class="teal-text text-lighten-1">' + data.y_open +'</span>');
                        $("p#close-pred").html('预测收盘价：<span class="teal-text text-lighten-1">' + data.y_close +'</span>');
                        $("p#delta-pred").html('预测涨跌：<span class="teal-text text-lighten-1">' + data.delta + ' (' + data.delta_percent + '%)</span>');
                        $("p#confidence-pred").html('涨跌置信度：<span class="teal-text text-lighten-1">' + data.senti +'</span>');
                    }
                    $("p#stock-name").html('<span>' + data.stockname + '</span>');
                    // 基于准备好的dom，初始化echarts实例
                    var myChart1 = echarts.init(document.getElementById('open-pic'));
                    // 指定图表的配置项和数据
                    option1 = {
                        title: {
                            text: '开盘价趋势'
                        },
                        tooltip: {
                            trigger: 'axis'
                        },
                        legend: {
                            data: ['实际股价', '预测股价']
                        },
                        grid: {
                            left: '3%',
                            right: '4%',
                            bottom: '3%',
                            containLabel: true
                        },
                        toolbox: {
                            feature: {
                                saveAsImage: {}
                            }
                        },
                        xAxis: {
                            type: 'category',
                            boundaryGap: false,
                            data: data.x_ax
                        },
                        yAxis: {
                            type: 'value',
                            min: data.ymin,
                            max: data.ymax
                        },
                        series: [
                            {
                                name: '实际股价',
                                type: 'scatter',
                                symbolSize: 6,
                                data: data.open
                            },
                            {
                                name: '预测股价',
                                type: 'line',
                                data: data.open_pred
                            },

                        ]
                    };
                    // 使用刚指定的配置项和数据显示图表。
                    myChart1.setOption(option1);

                    var myChart2 = echarts.init(document.getElementById('close-pic'));
                    // 指定图表的配置项和数据
                    option2 = {
                        title: {
                            text: '收盘价趋势'
                        },
                        tooltip: {
                            trigger: 'axis'
                        },
                        legend: {
                            data: ['实际股价', '预测股价']
                        },
                        grid: {
                            left: '3%',
                            right: '4%',
                            bottom: '3%',
                            containLabel: true
                        },
                        toolbox: {
                            feature: {
                                saveAsImage: {}
                            }
                        },
                        xAxis: {
                            type: 'category',
                            boundaryGap: false,
                            data: data.x_ax
                        },
                        yAxis: {
                            type: 'value',
                            min: data.ymin,
                            max: data.ymax
                        },
                        series: [
                            {
                                name: '实际股价',
                                type: 'scatter',
                                symbolSize: 6,
                                data: data.close
                            },
                            {
                                name: '预测股价',
                                type: 'line',
                                data: data.close_pred
                            },

                        ]
                    };
                    // 使用刚指定的配置项和数据显示图表。
                    myChart2.setOption(option2);
                }
            },
            error: function (error_data) {
                console.log("error");
                console.log(error_data);
            }
        })
    });