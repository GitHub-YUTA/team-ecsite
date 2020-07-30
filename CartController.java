package jp.co.internous.sky.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;

import jp.co.internous.sky.model.domain.TblCart;
import jp.co.internous.sky.model.domain.dto.CartDto;
import jp.co.internous.sky.model.form.CartForm;
import jp.co.internous.sky.model.mapper.TblCartMapper;
import jp.co.internous.sky.model.session.LoginSession;

@Controller
@RequestMapping("/sky/cart")
public class CartController {
	
	@Autowired
	protected LoginSession loginSession;
	
	@Autowired
	TblCartMapper tblCartMapper;
	
	Gson gson = new Gson();

//	カート画面表示
	@RequestMapping("/")
	public String index(Model m) {
		
//		①sessionからユーザーIDを取得
		int userId = loginSession.getLogined() ? loginSession.getUserId() : loginSession.getTmpUserId();
	
//		②表示用の情報を取得
		List<CartDto> cartList = tblCartMapper.findByUserId(userId);
		
//		③viewに渡す
		m.addAttribute("cartList", cartList);
		m.addAttribute("loginSession",loginSession);
		
//		④「cart.html」を表示
		return "cart";
	}
	
//	カート追加処理
	@RequestMapping("/add")
	public String add(CartForm cf, Model m) {
		
//		①sessionからユーザーIDを取得
		int userId = loginSession.getLogined() ? loginSession.getUserId() : loginSession.getTmpUserId();
		cf.setUserId(userId);
		
//		②CartFormからの情報をTblCartに置く
		TblCart tc = new TblCart(cf);

//		③すでに同名商品がカート内にある場合は更新、ない場合は追加
		if (tblCartMapper.findCountByUserIdAndProductId(userId, cf.getProductId()) > 0) {
			tblCartMapper.update(tc);
		} else {
			tblCartMapper.insert(tc);
		}
		
//		④表示用の情報を取得
		List<CartDto> cartList = tblCartMapper.findByUserId(userId);
		
//		⑤viewに渡す
		m.addAttribute("cartList",cartList);
		m.addAttribute("loginSession",loginSession);	
		
//		⑥「cart.html」を表示
		return "cart";
	}
	
//	カート削除処理
	@SuppressWarnings("unchecked")
	@ResponseBody
	@RequestMapping("/delete")
	public boolean deleteCart(@RequestBody String checkedIdList) {
		
		int result = 0;

		Map<String, List<String>> map = gson.fromJson(checkedIdList, Map.class);
		List<String> checkedIds = map.get("checkedIdList");
		for (String id : checkedIds) {
			result += tblCartMapper.deleteById(Integer.parseInt(id));
		}
		
		return result > 0;
	}
}