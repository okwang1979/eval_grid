(window["webpackJsonp_name_"] = window["webpackJsonp_name_"] || []).push([[1],{

/***/ 1998:
/***/ (function(module, exports, __webpack_require__) {

"use strict";


Object.defineProperty(exports, "__esModule", {
	value: true
});
exports.default = undefined;

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = __webpack_require__(0);

var _react2 = _interopRequireDefault(_react);

var _base = __webpack_require__(14);

var _toast = __webpack_require__(18);

var _toast2 = _interopRequireDefault(_toast);

var _method = __webpack_require__(2004);

__webpack_require__(2005);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _toConsumableArray(arr) { if (Array.isArray(arr)) { for (var i = 0, arr2 = Array(arr.length); i < arr.length; i++) { arr2[i] = arr[i]; } return arr2; } else { return Array.from(arr); } }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; } // 单据追溯图表


var G6 = __webpack_require__(2000);
var G6Plugins = __webpack_require__(2001);
// import '@antv/g6/build/plugin.behaviour.analysis';
G6.track(false);

var DagreChart = function (_Component) {
	_inherits(DagreChart, _Component);

	function DagreChart(props) {
		_classCallCheck(this, DagreChart);

		var _this = _possibleConstructorReturn(this, (DagreChart.__proto__ || Object.getPrototypeOf(DagreChart)).call(this, props));

		_this.getDrawDagreData = function (list) {
			var data = {
				nodes: list,
				edges: []
			};
			list.forEach(function (item) {
				var _data$edges;

				return (_data$edges = data.edges).push.apply(_data$edges, _toConsumableArray((0, _method.createEdges)(item)));
			});
			_this.setState({ data: data }, function () {
				_this.renderDagre(_this.changeColor(_this.state.data));
			});
		};

		_this.renderShape = function (G6, shape) {
			if (shape === 'react') {
				return G6.registerNode('rect', {
					getPath: function getPath() {
						var width = 136; // 一半宽
						var height = 60; // 一半高
						return G6.Util.getRectPath(-width / 2, -height / 2, width, height, 10);
					}
				});
			}
		};

		_this.initDagre = function () {
			var tooltipText = _this.props.tooltipText;

			_this.renderShape(G6, 'react');
			_this.dagre = new G6Plugins['layout.dagre']({
				rankdir: 'LR', // 左右方向
				nodesep: 30, // 节点间距
				ranksep: 30 // 层级间距
			});
			_this.tooltip = new G6.Plugins['tool.tooltip']({
				//每个节点的气泡提示
				getTooltip: function getTooltip(_ref) {
					var item = _ref.item;

					var model = item.getModel();
					if (model) {
						var title = model.billTypeName;
						var billcode = model.billCode;
						var transType = model.transTypeName;
						// let nameLabel = tooltipText && tooltipText[0] ? tooltipText[0] : '单据名称-1';
						var codeLabel = tooltipText && tooltipText[1] ? tooltipText[1] : '单据编号-1';
						var transTypeLabel = tooltipText && tooltipText[2] ? tooltipText[2] : '转单类型-1';
						var tips = '';
						if (title && billcode) {
							tips = '<ul class="dagre-toolTip">\n\t\t\t\t\t\t\t<li>' + title + '</li>\n\t\t\t\t\t\t\t<li>' + codeLabel + '</li>\n\t\t\t\t\t\t\t<li> ' + billcode + '</li>\n\t\t\t\t\t\t\t<li> ' + transTypeLabel + ':' + transType + '</li>\n\t\t\t\t\t\t</ul>';
						}
						return tips;
					}
				}
			});
			_this.graph = new G6.Graph({
				id: 'mountNode', //dom Id
				heigth: 250, // 网格的高度(不太管用，需进一步验证)
				plugins: [_this.dagre, _this.tooltip],
				defaultIntersectBox: 'rect', // 使用矩形包围盒
				modes: {
					default: ['panBlank', 'panNode', 'wheelZoom']
				}
			});
			//流程线条箭头设置
			_this.graph.edge({
				style: {
					endArrow: true
				},
				label: function label(model) {
					return model.shape;
				},

				size: 1
			});

			_this.graph.node({
				shape: 'rect',
				style: {
					stroke: '#CCEAFF'
				}
			});
			_this.itemClickEvent();
			_this.graph.on('wheel', function () {
				var wheelScrollZoom = _this.graph.getZoom();
				_this.setState({
					wheelScrollZoom: wheelScrollZoom
				});
			});
		};

		_this.calculaterGraphPosition = function (data) {
			var graph = _this.graph;
			/*
   sourceArr是节点sourceList和targetList的集合，数据套数据[[],[],...],
   流程图的列数graphColumnCount是sourceArr的长度加一，
   流程图的行数graphRowCount是sourceArr的子项最长一项的长度，
    */
			var sourceArr = [];
			var graphColumnCount = 0;
			var graphRowCount = 0;
			if (data.nodes.length > 1) {
				//流程图有多个节点的时候
				data.nodes.forEach(function (val) {
					if (val.source.length) {
						sourceArr.push(val.source);
					}
					if (val.target.length) {
						sourceArr.push(val.target);
					}
				});
				if (sourceArr.length) {
					graphColumnCount = sourceArr.length + 1;
					var lenArr = [];
					sourceArr.forEach(function (itemArr) {
						lenArr.push(itemArr.length);
					});
					lenArr.sort(function (a, b) {
						return b - a;
					});
					graphRowCount = lenArr[0];

					var graphWidth = graphColumnCount * 136 + (graphColumnCount - 1) * 30;
					var graphHeight = graphRowCount * 50 + (graphRowCount - 1) * 30;

					var dagreScrollWrap = document.querySelector('.dagreScrollWrap');
					if (dagreScrollWrap && _this.firstRender) {
						var dagreScrollWrapHeight = dagreScrollWrap.clientHeight;
						var dagreScrollWrapWidth = dagreScrollWrap.clientWidth;
						_this.dagreScrollWrapHeight = dagreScrollWrapHeight;
						_this.dagreScrollWrapWidth = dagreScrollWrapWidth;

						var dy = (dagreScrollWrapHeight - graphHeight) / 2;
						var dx = (dagreScrollWrapWidth - graphWidth) / 2;
						if (_this.dagreScrollWrapHeight) {
							if (dx >= 0 && dy >= 0) {
								graph.translate(dx, dy);
							} else if (dx >= 0 && dy <= 0) {
								graph.translate(dx, 0);
							} else if (dy >= 0 && dx <= 0) {
								graph.translate(0, dy);
							}
							_this.setState({
								graphWidth: graphWidth,
								graphHeight: graphHeight
							});
							_this.firstChangeZoom = true;
							_this.firstRender = false;
						}
					}
				}
			} else if (data.nodes.length === 1) {
				//只有一个节点的时候
				var _dagreScrollWrap = document.querySelector('.dagreScrollWrap');
				if (_dagreScrollWrap && _this.firstRender) {
					var _dagreScrollWrapHeight = _dagreScrollWrap.clientHeight;
					var _dagreScrollWrapWidth = _dagreScrollWrap.clientWidth;
					_this.dagreScrollWrapHeight = _dagreScrollWrapHeight;
					_this.dagreScrollWrapWidth = _dagreScrollWrapWidth;
					var _dy = (_dagreScrollWrapHeight - 60) / 2;
					var _dx = (_dagreScrollWrapWidth - 136) / 2;
					graph.translate(_dx, _dy);
					_this.setState({
						graphWidth: 136,
						graphHeight: 60
					});
					_this.firstRender = false;
					_this.firstChangeZoom = true;
				}
			}
		};

		_this.renderDagre = function (data) {
			//渲染出流程图会走两次这个方法
			_this.calculaterGraphPosition(data);

			var graph = _this.graph;
			(0, _method.dragNodeChangePostion)(graph);
			graph.read(data); //把绘制流程图的数据传进去
			if (graph && _this.currentZoom) {
				//图方法之后点击，保持点击前的大小
				graph.zoom(_this.currentZoom);
			}
		};

		_this.itemClickEvent = function () {
			_this.graph.on('click', function (ev) {
				if (ev.item && ev.item.isNode) {
					var selectedId = ev.item.id;
					var selectedType = ev.item.model.type;
					if (selectedId !== _this.state.selectedId) {
						_this.setState({ selectedId: selectedId, selectedType: selectedType }, function () {
							_this.props.getSelectId && _this.props.getSelectId(selectedId);
						});
						// if (this.state.extract) {
						// 	let data = this.changeColor(this.state.relatedData);
						// 	this.setState({ relatedData: data }, () => this.renderDagre(data));
						// } else {
						// 	let data = this.changeColor(this.state.data);
						// 	this.setState({ data }, () => this.renderDagre(data));
						// }
						var data = _this.changeColor(_this.state.data);
						_this.setState({ data: data }, function () {
							return _this.renderDagre(data);
						});
					}
				}
			});
			_this.graph.on('dblclick', function (ev) {
				if (ev.item && ev.item.type === 'node') {
					var selectedId = ev.item.model.id;
					var type = _this.state.data.nodes.find(function (elem) {
						return elem.id == selectedId;
					}).type;
					var transtype = _this.state.data.nodes.find(function (elem) {
						return elem.id == selectedId;
					}).transtype;
					_this.props.onSelect && _this.props.onSelect(selectedId, type, transtype);
				}
			});
		};

		_this.changeColor = function (data) {
			_this.currentZoom = _this.graph.getZoom();
			data.nodes.forEach(function (item) {
				if (item && item.color) {
					item.color = '#fff';
				}
			});
			data.edges.forEach(function (item) {
				if (item && item.color) {
					item.color = '#888';
				}
			});
			var selectedId = _this.state.selectedId;

			if (selectedId) {
				var relatedData = (0, _method.getRelated)(selectedId, _this.state.data); // 未抽取视图的相关联数据
				// let relatedData2 = extract ? getRelated(selectedId, relatedData) : { nodes: [], edges: [] }; // 抽取路径后视图的相关联数据
				var nodes = [].concat(_toConsumableArray(relatedData.nodes));
				var edges = [].concat(_toConsumableArray(relatedData.edges));
				if (nodes) {
					nodes.forEach(function (item) {
						if (item) {
							if (item.id === _this.state.selectedId) {
								if (item.label && item.label.fill) {
									item.label.fill = '#007ACE';
								}
								item.color = '#CCEAFF';
							} else {
								if (item.label && item.label.fill) {
									item.label.fill = '#F87321';
								}
								item.color = '#FFE19D';
							}
						}
					});
					edges.forEach(function (item) {
						if (item && item.color) {
							item.color = 'green';
						}
					});
				}
			}
			return data;
		};

		_this.handleClick = function () {
			var _this$props = _this.props,
			    extractTip = _this$props.extractTip,
			    renderNewData = _this$props.renderNewData,
			    extract = _this$props.extract,
			    originPk = _this$props.originPk,
			    originType = _this$props.originType;
			var _this$state = _this.state,
			    selectedId = _this$state.selectedId,
			    selectedType = _this$state.selectedType;

			if (!extract) {
				if (selectedId && selectedType) {
					renderNewData(selectedId, selectedType, true);
				} else {
					(0, _toast2.default)({ color: 'warning', content: extractTip });
				}
			} else {
				renderNewData(originPk, originType, false);
			}
		};

		_this.calculatePostionbBeforeRender = function (size) {
			var _this$state2 = _this.state,
			    graphHeight = _this$state2.graphHeight,
			    graphWidth = _this$state2.graphWidth,
			    wheelScrollZoom = _this$state2.wheelScrollZoom;

			setTimeout(function () {
				var dagreScrollWrap = document.querySelector('.dagreScrollWrap');
				if (dagreScrollWrap) {
					var dagreScrollHeight = dagreScrollWrap.clientHeight;
					var dagreScrollWidth = dagreScrollWrap.clientWidth;

					if (dagreScrollHeight - graphHeight >= 0) {
						//显示表格，且流程图高度小于于可视区
						if (!_this.dagreScrollH) {
							_this.dagreScrollH = dagreScrollHeight;
						} else {
							if (graphHeight) {
								if (size === false) {
									//显示表格的时候
									if (!_this.hasTableDy) {
										var dy2 = (dagreScrollHeight - graphHeight) / 2;
										var dy1 = (dagreScrollHeight * 2 - graphHeight) / 2;
										var dy = dy2 - dy1;
										_this.hasTableDy = dy2;
										_this.graph.translate(0, dy);
										_this.prevDy = dy;
									}
								}
							}
						}
					} else if (dagreScrollHeight - graphHeight < 0) {
						//流程图高度大于可视区
						if (dagreScrollHeight) {
							_this.dagreScrollH = graphHeight;
							if (_this.firstChangeZoom) {
								//在流程图的宽高大于dagreScrollWrap宽高的时候，缩放比例按照差值更多的一方来计算
								_this.firstChangeZoom = false;
								var zoomX = dagreScrollWidth / graphWidth;
								var zoomY = dagreScrollHeight / graphHeight;
								if (zoomX < zoomY) {
									_this.currentZoom = zoomX;
								} else {
									_this.currentZoom = zoomY;
								}
								if (wheelScrollZoom) {
									_this.graph.zoom(wheelScrollZoom);
								} else {
									_this.graph.zoom(_this.currentZoom);
								}
							}
						}
					}
					if (dagreScrollWidth - graphWidth >= 0) {
						//流程图宽度小于可视区
						if (!_this.dagreScrollW) {
							_this.dagreScrollW = dagreScrollWidth;
						}
					} else {
						if (!_this.dagreScrollW) {
							_this.dagreScrollH = graphWidth;
						}
					}
				}
			}, 50);
		};

		_this.state = {
			data: {
				nodes: _this.props.list,
				edges: (0, _method.createEdges)(_this.props.list)
			}, // 节点和线条数据
			selectedId: _this.props.selectedId || '', // 选中的节点id，页面初加载时选中节点为基本节点,即基于基本节点构建的上下游关系
			selectedType: '',
			// extract: false, // 是否是路径抽取状态
			relatedData: null, // 抽取路径后相关联的节点数据，用来渲染路径抽取视图
			graphHeight: 0, //流程图大于整个可视区的时候，图的高度
			graphWidth: 0, //流程图大于整个可视区的时候，图的宽度
			wheelScrollZoom: null //滚动鼠标滚轮之后的缩放比例
		};
		_this.dagre = null; //流程图
		_this.tooltip = null; //节点悬浮内容提示
		_this.graph = null;
		_this.currentZoom = null; //渲染前的画布比例
		_this.firstRender = true;
		_this.dagreScrollH = null; //dagreScroll的高度
		_this.dagreScrollW = null; //dagreScroll的宽度
		_this.dagreScrollWrapHeight = 0;
		_this.dagreScrollWrapWidth = 0;
		_this.firstChangeZoom = false; //打开单据追溯弹框之后，第一次渲染流程图的时候计算zoom，之后不计算
		_this.hasTableDy = null; //显示隐藏表格的时候，值计算一次位移，在流程图小于可视区的时候，在显示表格的时候高度居中
		_this.prevDy = null; //流程图前一次计算的Y轴的位移，是对应modal的body上边而言
		return _this;
	}

	_createClass(DagreChart, [{
		key: 'componentWillMount',
		value: function componentWillMount() {
			var _props = this.props,
			    list = _props.list,
			    parentCom = _props.parentCom;

			if (parentCom !== 'BillTrack') {
				//父组件不是单据追溯的时候，初始化接收渲染图的数据
				this.getDrawDagreData(list);
			}
		}
	}, {
		key: 'componentDidMount',
		value: function componentDidMount() {
			var getSelectId = this.props.getSelectId;

			this.initDagre();
			if (getSelectId && typeof getSelectId === 'function') {
				getSelectId(this.state.selectedId);
			}
			// let modalBody = document.querySelector('.u-modal-bill-track-body');
			// if (modalBody) {
			// 	modalBody.style.minHeight = modalBody.clientHeight + 'px';
			// 	modalBody.style.minWidth = modalBody.clientWidth + 'px';
			// }
		}
	}, {
		key: 'componentWillReceiveProps',
		value: function componentWillReceiveProps(nextProps) {
			var list = nextProps.list,
			    size = nextProps.size;

			this.getDrawDagreData(list);
			if (size !== undefined && size !== this.props.size) {
				if (size == true) {
					//隐藏表格 图下移
					this.graph.translate(0, -this.prevDy);
				} else {
					//显示表格 图上移
					this.graph.translate(0, this.prevDy);
				}
			}
		}

		/**接收渲染图的数据
   * list:array 是BIltrack的index.js中的dataFormat方法返回的数组
   */


		/**初始化渲染并绑定节点事件
   * tooltipText:Array 流程图节点悬停的气泡提示中文
   */


		//计算流程图的宽高，用途：让流程图居中显示 g6没有这个功能的API


		// 渲染关系图


		// 节点的点击事件


		/**
   * 改变选中节点的颜色
   */


		// 抽取路径按钮的点击事件


		//每次流程图渲染的时候，让图中部居中

	}, {
		key: 'render',
		value: function render() {
			//size 表格是否显示的控制参数 Boolean  默认是false不显示表格 true是显示表格
			var _props2 = this.props,
			    size = _props2.size,
			    extractBtns = _props2.extractBtns,
			    extract = _props2.extract;


			var btnTitle = void 0;
			if (extractBtns) {
				btnTitle = extract ? extractBtns[0] : extractBtns[1];
			}
			this.calculatePostionbBeforeRender(size);
			return _react2.default.createElement(
				'div',
				{
					className: (this.props.className ? this.props.className : '') + ' dagre-area ',
					style: { height: '' + (size ? '96%' : '50%') }
				},
				btnTitle && _react2.default.createElement(
					_base.NCButton,
					{
						className: 'button-primary dagre-extract',
						fieldid: extract ? 'show-all' : 'dagre-extract',
						onClick: this.handleClick
					},
					btnTitle
				),
				_react2.default.createElement(
					'div',
					{ className: 'dagreScrollWrap' },
					_react2.default.createElement(
						'div',
						{
							className: 'dagreScroll',
							style: {
								//单据追溯点击节点，数据量大时会出现模糊现象，先这样修改，有问题再说
								height: '100%',
								width: '100%'
							}
						},
						_react2.default.createElement('div', { id: 'mountNode', className: 'dagre', style: { width: '100%' } })
					)
				)
			);
		}
	}]);

	return DagreChart;
}(_react.Component);

exports.default = DagreChart;

/***/ }),

/***/ 2004:
/***/ (function(module, exports, __webpack_require__) {

"use strict";


Object.defineProperty(exports, "__esModule", {
    value: true
});

function _toConsumableArray(arr) { if (Array.isArray(arr)) { for (var i = 0, arr2 = Array(arr.length); i < arr.length; i++) { arr2[i] = arr[i]; } return arr2; } else { return Array.from(arr); } }

/**
 * 节点信息生成
 * **/
var flowFormatNode = function flowFormatNode(data) {
    return data.map(function (item, index) {
        return {
            id: item.act_id,
            label: {
                text: item.label
            },
            name: item.act_name
        };
    });
};

/**
 * 流程节点关系数组生成
 * **/
var createEdges = function createEdges(node) {
    switch (node.isForward) {
        case '0':
            // 0表示节点是当前节点，当前节点找上下游
            var sourceEdges = node.source.map(function (item) {
                return { source: item, target: node.id };
            });
            var targetEdges = node.target.map(function (item) {
                return { source: node.id, target: item };
            });
            return [].concat(_toConsumableArray(sourceEdges), _toConsumableArray(targetEdges));
            break;
        case '1':
            // 1表示下游节点，下游节点找下游
            return node.target.map(function (item) {
                return { source: node.id, target: item };
            });
            break;
        case '-1':
            // -1表示上游节点，上游节点找上游
            return node.source.map(function (item) {
                return { source: item, target: node.id };
            });
            break;
        default:
            return [];
            break;
    }
};
/**
 * 节点简单拖拽位置变化
 * **/
var dragNodeChangePostion = function dragNodeChangePostion(graph) {
    var node = void 0;
    var dx = void 0;
    var dy = void 0;
    graph.on('node:dragstart', function (ev) {
        var item = ev.item;

        var model = item.getModel();
        node = item;
        dx = model.x - ev.x;
        dy = model.y - ev.y;
    });
    graph.on('node:drag', function (ev) {
        node && graph.update(node, {
            x: ev.x + dx,
            y: ev.y + dy
        });
    });
    graph.on('node:dragend', function (ev) {
        node = undefined;
    });
};

/**
 * 生成初始化的当前所属节点
 * **/
var generateInitSelected = function generateInitSelected(data) {
    return data.filter(function (item, index) {
        return item.ispassroute;
    });
};

/**
 * 判断两个对象数组是否相等 【升级之后没有用的】
 * **/
var equalArray = function equalArray(arr1, arr2) {
    if (!arr1 || !arr2) return false;
    if (Object.prototype.toString.call(arr1).slice(8, -1) !== 'Array' || Object.prototype.toString.call(arr2).slice(8, -1) !== 'Array') return false;
    return JSON.stringify(arr1) === JSON.stringify(arr2);
};

/**
 * 查找上下游的节点和路径 参数data: 查找相关数据的原始数据范围
 */
var getRelated = function getRelated(keyNodeId, data) {
    var edges = [];
    var sources = [];
    var targets = [];
    var getSource = function getSource(id) {
        var _edges;

        var sourceEdges = data.edges.filter(function (_ref) {
            var target = _ref.target;
            return target === id;
        });
        var sourceIds = sourceEdges.map(function (item) {
            return item.source;
        });
        (_edges = edges).push.apply(_edges, _toConsumableArray(sourceEdges));
        sources.push.apply(sources, _toConsumableArray(sourceIds));
        if (sourceIds.length > 0) {
            sourceIds.forEach(function (item) {
                return getSource(item);
            });
        }
    };
    var getTargets = function getTargets(id) {
        var _edges2;

        var targetEdges = data.edges.filter(function (_ref2) {
            var source = _ref2.source;
            return source === id;
        });
        var targetIds = targetEdges.map(function (item) {
            return item.target;
        });
        (_edges2 = edges).push.apply(_edges2, _toConsumableArray(targetEdges));
        targets.push.apply(targets, _toConsumableArray(targetIds));
        if (targetIds.length > 0) {
            targetIds.forEach(function (item) {
                return getTargets(item);
            });
        }
    };
    getSource(keyNodeId);
    getTargets(keyNodeId);
    edges = [].concat(_toConsumableArray(new Set(edges)));
    var nodeIds = [].concat(_toConsumableArray(new Set([].concat(sources, [keyNodeId], targets)))); // 用 Set 去重
    var nodes = nodeIds.map(function (item) {
        return data.nodes.find(function (ele) {
            return ele.id === item;
        });
    });
    return {
        edges: edges,
        nodes: nodes
    };
};
exports.flowFormatNode = flowFormatNode;
exports.createEdges = createEdges;
exports.dragNodeChangePostion = dragNodeChangePostion;
exports.generateInitSelected = generateInitSelected;
exports.equalArray = equalArray;
exports.getRelated = getRelated;

/***/ }),

/***/ 2005:
/***/ (function(module, exports, __webpack_require__) {

!function(t,o){ true?module.exports=o(__webpack_require__(2000)):undefined}(window,function(t){return function(t){var o={};function e(n){if(o[n])return o[n].exports;var i=o[n]={i:n,l:!1,exports:{}};return t[n].call(i.exports,i,i.exports,e),i.l=!0,i.exports}return e.m=t,e.c=o,e.d=function(t,o,n){e.o(t,o)||Object.defineProperty(t,o,{enumerable:!0,get:n})},e.r=function(t){"undefined"!=typeof Symbol&&Symbol.toStringTag&&Object.defineProperty(t,Symbol.toStringTag,{value:"Module"}),Object.defineProperty(t,"__esModule",{value:!0})},e.t=function(t,o){if(1&o&&(t=e(t)),8&o)return t;if(4&o&&"object"==typeof t&&t&&t.__esModule)return t;var n=Object.create(null);if(e.r(n),Object.defineProperty(n,"default",{enumerable:!0,value:t}),2&o&&"string"!=typeof t)for(var i in t)e.d(n,i,function(o){return t[o]}.bind(null,i));return n},e.n=function(t){var o=t&&t.__esModule?function(){return t.default}:function(){return t};return e.d(o,"a",o),o},e.o=function(t,o){return Object.prototype.hasOwnProperty.call(t,o)},e.p="",e(e.s=133)}({133:function(t,o,e){var n=Object.assign||function(t){for(var o=1;o<arguments.length;o++){var e=arguments[o];for(var n in e)Object.prototype.hasOwnProperty.call(e,n)&&(t[n]=e[n])}return t};var i=e(3),r=i.Util,p=function(){function t(o){!function(t,o){if(!(t instanceof o))throw new TypeError("Cannot call a class as a function")}(this,t),this.options=n({dx:10,dy:10,getTooltip:function(t){var o=t.item;if(o){var e=o.getModel().tooltip;if(e){var n="",i=o.type;return r.isArray(e)?e.forEach(function(t){n+='<li style="font-size: 12px;list-style-type: none;"><span>'+t[0]+"</span>: "+t[1]+"</li>"}):(i=e.title,e.list.forEach(function(t){n+='<li style="font-size: 12px;list-style-type: none;"><span>'+t[0]+"</span>: "+t[1]+"</li>"})),'\n              <div class="g6-tooltip" style="\n                position: absolute;\n                white-space: nowrap;\n                zIndex: 8;\n                transition: visibility 0.2s cubic-bezier(0.23, 1, 0.32, 1), left 0.4s cubic-bezier(0.23, 1, 0.32, 1), top 0.4s cubic-bezier(0.23, 1, 0.32, 1);\n                background-color: rgba(255, 255, 255, 0.9);\n                box-shadow: 0px 0px 10px #aeaeae;\n                border-radius: 3px;\n                color: rgb(87, 87, 87);\n                line-height: 20px;\n                padding: 10px 10px 6px 10px;\n              ">\n                <h4 class="g6-tooltip-title" style="\n                  margin: 0;\n                ">'+i+'</h4>\n                <ul class="g6-tooltip-list" style="\n                  padding: 0;\n                  margin: 0;\n                  margin-top: 4px;\n                ">\n                  '+n+"\n                </ul>\n              </div>\n            "}}return null}},o)}return t.prototype.init=function(){var t=this,o=this.graph;o.on("mouseenter",function(o){o.shape&&t.onMouseEnter(o)}),o.on("mousemove",function(o){t.onMouseMove(o)}),o.on("mouseleave",function(o){t.onMouseLeave(o)})},t.prototype.onMouseEnter=function(t){var o=this.graph.getGraphContainer(),e=this.options.getTooltip(t),n=this.tooltip;n&&this.tooltip.destroy(),e?((n=r.createDOM(e)).css({position:"absolute"}),o.appendChild(n)):n=null,this.tooltip=n},t.prototype.onMouseMove=function(t){var o=t.domX,e=t.domY,n=this.tooltip;if(n){var i=this.graph,p=r.getOuterWidth(n),s=r.getOuterHeight(n),l=i.getWidth(),u=i.getHeight();n.css({top:this._getTop(s,u,e),left:this._getLeft(p,l,o)})}},t.prototype.onMouseLeave=function(){this.tooltip&&(this.tooltip.destroy(),this.tooltip=null)},t.prototype._getTop=function(t,o,e){var n=this.options.dy;return 2*t>=o?"0px":e<t+n?e+n+"px":e-t-n+"px"},t.prototype._getLeft=function(t,o,e){var n=this.options.dx;return 2*t>=o?"0px":o-e-n<t?e-t-n+"px":e+n+"px"},t.prototype.destroy=function(){this.tooltip&&this.tooltip.destroy()},t}();i.Plugins["tool.tooltip"]=p,t.exports=p},3:function(o,e){o.exports=t}})});
//# sourceMappingURL=plugin.tool.tooltip.js.map

/***/ })

}]);