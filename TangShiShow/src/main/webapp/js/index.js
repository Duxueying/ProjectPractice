// var 是 variable
// document 是 js 中的 html 文档对象
// document.getElementById('main')) 找到 html 文档中的 id 为 main 的元素
// echarts.init 初始化画布元素
$.ajax(
    {
        method: "get",  // 发起 ajax 请求时，使用什么 http 方法
        url: "rank.json?condition=10",   // 请求哪个 url
        dataType: "json",   // 返回的数据当成什么格式解析
        success: function (data) {  // 成功后，执行什么方法
            var names = [];
            var counts = [];

            for (var i in data) {
                names.push(data[i][0]);
                counts.push(data[i][1]);
            }

            console.log(names);
            console.log(counts);
            var myChart = echarts.init(document.getElementById('main'));

            var option = {
                // 图标的标题
                title: {
                    text: '诗人创作排行榜'
                },
                tooltip: {},
                legend: {
                    data:['销量']
                },
                // 横坐标
                xAxis: {
                    data: names
                },
                yAxis: {},
                series: [
                    {
                        name: '销量',
                        type: 'bar',    // bar 代表柱状图
                        itemStyle: {
                            color: new echarts.graphic.LinearGradient(
                                0, 0, 0, 1,
                                [
                                    {offset: 0, color: '#83bff6'},
                                    {offset: 0.5, color: '#188df0'},
                                    {offset: 1, color: '#188df0'}
                                ]
                            )
                        },
                        emphasis: {
                            itemStyle: {
                                color: new echarts.graphic.LinearGradient(
                                    0, 0, 0, 1,
                                    [
                                        {offset: 0, color: '#2378f7'},
                                        {offset: 0.7, color: '#2378f7'},
                                        {offset: 1, color: '#83bff6'}
                                    ]
                                )
                            }
                        },
                        data: counts
                    }
                ]
            };

            myChart.setOption(option);
        }
    }
);


// 当前的数据全部是在前端写死的，我需要通过一个 http 请求，从后端获取所有数据
// jquery
