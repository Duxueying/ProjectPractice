$.ajax({
    method: "get",
    url: "words.json",
    dataType: "json",
    success: function (jsonData) {
        var myChart = echarts.init(document.getElementById('main'));

        var data = [];
        for (var i in jsonData) {
            var word = jsonData[i][0];
            var count = jsonData[i][1];
            data.push({
                name: word,
                value: count,
                textStyle: {
                    normal: {},
                    emphasis: {}
                }
            });
        }
        console.log(data);
        var option = {
            series: [{
                type: 'wordCloud',
                shape: 'pentagon',
                left: 'center',
                top: 'center',
                width: '80%',
                height: '80%',
                right: null,
                bottom: null,
                sizeRange: [12, 60],
                rotationRange: [-90, 90],
                rotationStep: 45,
                gridSize: 8,
                drawOutOfBound: false,
                textStyle: {
                    normal: {
                        fontFamily: 'sans-serif',
                        fontWeight: 'bold',
                        color: function () {
                            return 'rgb(' + [
                                Math.round(Math.random() * 160),
                                Math.round(Math.random() * 160),
                                Math.round(Math.random() * 160)
                            ].join(',') + ')';
                        }
                    },
                    emphasis: {
                        shadowBlur: 10,
                        shadowColor: '#333'
                    }
                },
                data: data
            }]
        };

        myChart.setOption(option);
    }
});

// $.ajax(
//     {
//         method:"get",//Ajax请求时，使用什么http方法
//         url:"words.json",//请求哪个url
//         dataType:"json",//返回的数据当成什么格式解析
//         success:function (data) {//成功后执行什么方法
//                 var names=[];
//                 var counts=[];
//
//                 for(var i in data){
//                     names.push(data[i][0]);
//                     counts.push(data[i][1]);
//                 }
//
//
//                 //echarts
//                 var myChart = echarts.init(document.getElementById('main'));
//
//                 // 指定图表的配置项和数据
//                 var option = {
//                     // 图标的标题
//                     title: {
//                         text: '唐诗可视化'
//                     },
//                     tooltip: {},
//                     legend: {
//                         data:['数量']
//                     },
//                     // 横坐标
//                     xAxis: {
//                         data: names
//                     },
//                     //总左边
//                     yAxis: {},
//                     series: [{
//                         name: 'shul',//代表的是什么
//                         type: 'bar',    // bar 代表柱状图
//                         data: counts// 对应的每一个的数据
//
//                     }]
//                 };
//
//                 myChart.setOption(option);//做到关联
//
//             }
//         }
// )