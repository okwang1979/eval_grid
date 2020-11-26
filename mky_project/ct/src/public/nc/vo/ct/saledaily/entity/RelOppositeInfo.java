package nc.vo.ct.saledaily.entity;

import java.io.Serializable;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class RelOppositeInfo implements Serializable{
	
	@NotNull
	@Size(max = 50)
	private String oppositeUniqueId;
	
	
	@NotNull
	@Size(max = 100)
	private String oppositeName;
	
	
	@NotNull
	@Size(max = 50)
	private String oppositeRelName;
	
	@Size(max = 100)
	private String bankOfDeposit;
	@Size(max = 100)
	private String bankAccount;
	
	@NotNull
	@Size(max = 100)
	private String bankAccountName;

	public String getOppositeUniqueId() {
		return oppositeUniqueId;
	}

	public void setOppositeUniqueId(String oppositeUniqueId) {
		this.oppositeUniqueId = oppositeUniqueId;
	}

	public String getOppositeName() {
		return oppositeName;
	}

	public void setOppositeName(String oppositeName) {
		this.oppositeName = oppositeName;
	}

	public String getOppositeRelName() {
		return oppositeRelName;
	}

	public void setOppositeRelName(String oppositeRelName) {
		this.oppositeRelName = oppositeRelName;
	}

	public String getBankOfDeposit() {
		return bankOfDeposit;
	}

	public void setBankOfDeposit(String bankOfDeposit) {
		this.bankOfDeposit = bankOfDeposit;
	}

	public String getBankAccount() {
		return bankAccount;
	}

	public void setBankAccount(String bankAccount) {
		this.bankAccount = bankAccount;
	}

	public String getBankAccountName() {
		return bankAccountName;
	}

	public void setBankAccountName(String bankAccountName) {
		this.bankAccountName = bankAccountName;
	}
	
	
	
	
//	"oppositeUniqueId":"相对方唯一标识",
//	"oppositeName":"相对方名称",
//	"oppositeRelName":"相对方联系人",
//	"bankOfDeposit":"银行名称",
//	"bankAccount":" 银行账号",
//	"bankAccountName":"银行账户名"}],


}
