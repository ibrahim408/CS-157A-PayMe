package com.cs157a1.payMe.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import java.util.*;

import com.cs157a1.payMe.Entity.CreditCard;
import com.cs157a1.payMe.Model.CreditCardDao;

@Service
public class CreditCardsServices {
	
	@Autowired
	@Qualifier("CreditCardDao")
	private CreditCardDao creditCardDao;
	
	public List<CreditCard> returnAllInfo(){
		return creditCardDao.returnAllInfo();
	}
	
	public CreditCard returncreditCardBycardNumber(long l) {
		return creditCardDao.returncreditCardBycardNumber(l);
	}
	
	public void addcreditCardToDB(CreditCard creditCard,String username) {
		 creditCardDao.addcreditCardToDB(creditCard,username);
	}
	
	public void deletecreditCard(long cardNumber) {
		 creditCardDao.deletecreditCard(cardNumber);
	}
		
	public void updatecreditCard(CreditCard creditCard) {
		 creditCardDao.updatecreditCard(creditCard);
	}

}
