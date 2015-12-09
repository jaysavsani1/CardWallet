package com.quixom.cardwallet.library;

import java.io.Serializable;

public class CreditCard implements Serializable {
	
	private final String cardNumber;
	private final CardType cardType;


	public CreditCard(String cardNumber, CardType cardType) {
		this.cardNumber = cardNumber;
		this.cardType = cardType;
	}

	@SuppressWarnings("unused")
	public String getCardNumber() {
		return cardNumber;
	}

	@SuppressWarnings("unused")
	public CardType getCardType() {
		return cardType;
	}


	@Override
	public String toString() {

		final StringBuilder sb = new StringBuilder("CreditCard{");
		sb.append("cardNumber='").append(cardNumber).append('\'');
		sb.append(", cardType=").append(cardType);
		sb.append('}');
		return sb.toString();
	}
}
