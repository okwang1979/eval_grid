var assert = require('assert');

describe('Array', function() {
  describe('#indexOf()', function() {
    it('should return -1 when the value is not present', function() {
       
      assert.equal([1,2,3].indexOf(4), -1);
    });
  });
});

let {let_use} = require("../javascript-base/let_use")
describe('java-base',function(){
    it('let_use.js',
        function(){
            let_use();
            assert.equal(1,1);
        }

    );
    //解构
    let {dest} = require("../javascript-base/destructuring");
    it('destructuring',function(){
        dest();
    });

    //字符串
    let {test_str} = require("../javascript-base/test_string");
    it("test_string",function(){
      test_str();
    });

});

