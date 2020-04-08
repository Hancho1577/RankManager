package rankManager.listener;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.block.SignChangeEvent;
import cn.nukkit.event.player.PlayerChatEvent;
import cn.nukkit.event.player.PlayerFormRespondedEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.event.player.PlayerKickEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.form.element.Element;
import cn.nukkit.form.element.ElementButton;
import cn.nukkit.form.element.ElementInput;
import cn.nukkit.form.element.ElementLabel;
import cn.nukkit.form.window.FormWindowCustom;
import cn.nukkit.form.window.FormWindowModal;
import cn.nukkit.form.window.FormWindowSimple;
import cn.nukkit.utils.TextFormat;
import me.onebone.economyapi.EconomyAPI;
import rankManager.RankManager;
import rankManager.listener.other.ListenerLoader;
import rankManager.rank.RankData;
import rankManager.rank.RankLoader;
import rankManager.rank.RankProvider;

public class EventListener implements Listener {
	private static final SimpleDateFormat sdf = new SimpleDateFormat(
			"yyyy년 MM월 dd일 HH시 mm분 ss초");
	private RankManager plugin;
	private RankLoader loader;
	private RankProvider provider;
	private ListenerLoader listenerLoader;
	String stringz;
	LinkedHashMap<String, String> uiPrefixSet = new LinkedHashMap<>();
	LinkedHashMap<String, String> uiSeeOtherPlayersPrefix = new LinkedHashMap<>();
	LinkedHashMap<String, String> uiDeletePrefix = new LinkedHashMap<>();
	LinkedHashMap<String, String> uiAdminDeletePrefix = new LinkedHashMap<>();
	LinkedHashMap<String, String> uiAdminDeleteSelPrefix = new LinkedHashMap<>();
	LinkedHashMap<String, String> uiAdminChangePrefix = new LinkedHashMap<>();
	LinkedHashMap<String, Integer> uiSaveCostForSelling = new LinkedHashMap<>();
	LinkedHashMap<String, String> selectedSeller = new LinkedHashMap<>();

	public EventListener(RankManager plugin) {
		this.plugin = plugin;
		this.loader = plugin.getRankLoader();
		this.provider = plugin.getRankProvider();
		this.listenerLoader = plugin.getListenerLoader();

		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	public final boolean isNumeric(String s) {
		try {
			Double.parseDouble(s);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	public final String numberToKorean(int num) {
		/*
		 * final char[] unitWords = {' ','만','억'}; final int splitCount =
		 * unitWords.length; Map<Integer,Double> resultArray = new LinkedHashMap<>();
		 * String resultString = "";
		 * 
		 * for (int i = 0; i < splitCount; i ++) { Double unitResult = Math.floor((num %
		 * Math.pow(10000, i + 1)) / Math.pow(10000, i)); if(unitResult > 0) {
		 * resultArray.put(i,unitResult); } }
		 * 
		 * for(int i = 0; i < resultArray.size(); i++) { if(!resultArray.containsKey(i))
		 * continue; resultString = resultArray.get(i) + unitWords[i] + resultString; }
		 * return resultString;
		 */
		DecimalFormat df = new DecimalFormat("#,###");
		return df.format(num);
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		RankData rankData = this.loader.getRank(e.getPlayer());
		if (this.provider.getDefaultPrefix() != null) {
			if (!rankData.isExistPrefix(this.provider.getDefaultPrefix())) {
				rankData.addPrefixs(new String[] { this.provider.getDefaultPrefix() }, 1);
			}
			rankData.setPrefix(this.provider.getDefaultPrefix());
		}
		this.provider.applyNameTag(e.getPlayer().getName());
	}

	@EventHandler
	public void onPlayerQuitEvent(PlayerQuitEvent e) {
		this.loader.unloadRank(e.getPlayer().getName());
	}

	@EventHandler
	public void onPlayerKickEvent(PlayerKickEvent e) {
		this.loader.unloadRank(e.getPlayer().getName());
	}

	@EventHandler
	public void onPlayerChatEvent(PlayerChatEvent e) {
		e.setFormat(this.provider.applyChatFormat(e.getPlayer().getName(), "§7" + e.getMessage()));
	}

	@EventHandler
	public void onSignChangeEvent(SignChangeEvent e) {
		if (!e.getPlayer().hasPermission("rankmanager.rankshop.create"))
			return;
		String line = e.getLine(0);
		if (this.plugin.get("rankshop").equals(line)) {
			if (e.getLine(1) == null || e.getLine(2) == null || !isNumeric(e.getLine(2))) {
				this.plugin.message(e.getPlayer(), this.plugin.get("rankshop-help"));
				return;
			}
			if (e.getLine(3) == null || !isNumeric(e.getLine(3))) {
				this.plugin.message(e.getPlayer(),
						"4번째 줄엔 §6가치값§f를 적어주세요.\n가치값는 칭호를 판매할 때 사용하여 얻기 어려울수록 §6높은§f 값을 가져야합니다.\n최대 §6500만§f 까지 허용됩니다.\n§c칭호상점의 경우 가치값과 판매가를 같게 설정하세요");
				return;
			}
			if (Integer.parseInt(e.getLine(3).trim()) < 1 || Integer.parseInt(e.getLine(3).trim()) > 5000000) {
				this.plugin.message(e.getPlayer(), "4번째 줄의 가치값이 1원보다 작거나 5백만원을 초과합니다.");
				return;
			}
			String requestedPrefix = e.getLine(1);
			int requestedPrice = Integer.parseInt(e.getLine(2).trim());
			String levelName = e.getBlock().getLevel().getName();
			double x = e.getBlock().getX();
			double y = e.getBlock().getY();
			double z = e.getBlock().getZ();
			this.provider.setRankShop(levelName, x, y, z, requestedPrefix, requestedPrice,
					Integer.parseInt(e.getLine(3).trim()));
			String prefix = this.provider.applyPrefixFormat(requestedPrefix);
			e.setLine(0, this.plugin.get("rankshop-format1"));
			e.setLine(1, StringUtils.replace(this.plugin.get("rankshop-format2"), "%prefix%", prefix, 1));
			e.setLine(2, StringUtils.replace(this.plugin.get("rankshop-format3"),"%price%", numberToKorean(requestedPrice), 1));
			e.setLine(3, "§g§l가치값 §f: " + e.getLine(3).trim());
		}
	}

	@EventHandler
	public void onBlockBreakEvent(BlockBreakEvent event) {
		String levelName = event.getBlock().getLevel().getName();
		double x = event.getBlock().getX();
		double y = event.getBlock().getY();
		double z = event.getBlock().getZ();
		Map<String, Object> rankShop = this.provider.getRankShop(levelName, x, y, z);

		if (rankShop != null) {
			if (!event.getPlayer().hasPermission("rankmanager.rankshop.delete")) {
				event.setCancelled();
				this.plugin.alert(event.getPlayer(), this.plugin.get("you-cant-break-rank-shop"));
				return;
			}
			this.provider.deleteRankShop(levelName, x, y, z);
			this.plugin.message(event.getPlayer(), this.plugin.get("rank-shop-deleted"));
		}
	}

	@EventHandler
	public void onPlayerInteractEvent(PlayerInteractEvent event) {
		String levelName = event.getBlock().getLevel().getName();
		double x = event.getBlock().getX();
		double y = event.getBlock().getY();
		double z = event.getBlock().getZ();
		Map<String, Object> rankShop = this.provider.getRankShop(levelName, x, y, z);

		if (rankShop == null)
			return;

		event.setCancelled();
		if (!event.getPlayer().hasPermission("rankmanager.rankshop.use")) {
			this.plugin.alert(event.getPlayer(), this.plugin.get("rankshop-you-cant-buy-rank"));
			return;
		}

		EconomyAPI economyAPI = this.listenerLoader.getEconomyAPI();
		if (economyAPI == null) {
			this.plugin.alert(event.getPlayer(), this.plugin.get("there-are-no-economyapi"));
			return;
		}

		double myMoney = economyAPI.myMoney(event.getPlayer());
		if ((Integer) rankShop.get("price") > myMoney) {
			this.plugin.message(event.getPlayer(), this.plugin.get("rankshop-not-enough-money"));
			return;
		}

		RankData rankData = this.loader.getRank(event.getPlayer());
		if (rankData.isExistPrefix((String) rankShop.get("prefix"))) {
			this.plugin.alert(event.getPlayer(), this.plugin.get("already-buy-that-prefix"));
			this.plugin.alert(event.getPlayer(), this.plugin.get("you-can-change-prefix"));
			return;
		}

		economyAPI.reduceMoney(event.getPlayer(), (Integer) rankShop.get("price"));
		String[] temp = { (String) rankShop.get("prefix") };
		rankData.addPrefixs(temp, (Integer) rankShop.get("prePrice"));
		rankData.setPrefix((String) rankShop.get("prefix"));
		this.plugin.message(event.getPlayer(), this.plugin.get("prefix-buy-success"));
		this.plugin.message(event.getPlayer(), this.plugin.get("you-can-use-rank-set"));
	}

	/**
	 * @폼
	 */

	public void sendSimpleForm(Player player, String title, String content) {
		FormWindowSimple wd = new FormWindowSimple(title, content);
		player.showFormWindow(wd);
		return;
	}
	
	public void showSellingList(Player player) {

		LinkedHashMap<String, LinkedHashMap<String, Object>> list = this.provider.getSellingList();
		if (list.size() < 1) {
			this.sendSimpleForm(player, "§l§8칭호 거래소",
					"§f§l현재 §6판매중인§f 칭호가 없습니다!\n§r§f칭호를 판매하려면 §6칭호 목록§f에서 원하는 칭호를 선택하고 §6판매하기 §f버튼을 터치하세요!");
			return;
		}
		ArrayList<ElementButton> buttons = new ArrayList<>();

		list.forEach((key, data) -> {
			/*
			 * data.put("cost", cost); data.put("price", price); data.put("prefix", prefix);
			 * data.put("date", (int) (new Date().getTime()/1000));
			 */
			String lefttime = "";
			int date = (int) data.get("date") - (int) (new Date().getTime() / 1000);
			if ((date / 3600) > 0) {
				lefttime = "§6" + (int) Math.floor(date / 3600) + "§f시간 §6" + (int) Math.floor((date % 3600) / 60)
						+ "§f분";
			} else if ((date / 60) > 0) {
				lefttime = "§c" + (int) Math.floor((date % 3600) / 60) + "§f분";
			}
			buttons.add(new ElementButton("§8§l칭호 §f: §r" + data.get("prefix") + "\n§r§l§8판매가 : §6"
					+ numberToKorean((int) data.get("price")) + "\n§8판매자 §f: "+ key +"\n남은 시간 : " + lefttime));
		});
		FormWindowSimple aSimpleForm = new FormWindowSimple("§l§8칭호 거래소",
				"§f§l총 §6" + list.size() + "§f개의 칭호가 등록되어 있습니다.", buttons);
		player.showFormWindow(aSimpleForm, 9927);
	}
	
	public void sendMainUI(Player player) {
		ArrayList<ElementButton> buttons = new ArrayList<>();
		buttons.add(new ElementButton("§l§8칭호 목록"));
		// buttons.add(new ElementButton("§l§8칭호 변경"));
		buttons.add(new ElementButton("§l§8칭호 삭제"));
		buttons.add(new ElementButton("§l§8칭호 확인"));
		buttons.add(new ElementButton("§l§8칭호 거래소"));
		if (player.hasPermission("rankmanager.rank.control")) {
			buttons.add(new ElementButton("§l§8칭호 추가(관리자)"));
			buttons.add(new ElementButton("§l§8칭호 삭제(관리자)"));
			buttons.add(new ElementButton("§l§8칭호 변경(관리자)"));
			buttons.add(new ElementButton("§l§8닉네임 변경(관리자)"));
		}
		FormWindowSimple window = new FormWindowSimple("§8칭호", "§f§6TIP §f: /칭호 ? 명령어로 칭호 명령어를 사용할 수 있습니다.", buttons);
		player.showFormWindow(window, 1577);
	}

	public void sendShowPlayerPrefixs(Player player) {
		ElementInput input = new ElementInput("", "플레이어 이름 입력");
		ElementLabel label = new ElementLabel("§f특정 플레이어의 칭호목록을 확인할 수 있습니다.");
		FormWindowCustom newWindow = new FormWindowCustom("§l§8칭호 확인");
		newWindow.addElement(label);
		newWindow.addElement(input);
		player.showFormWindow(newWindow, 1579);
	}

	public void showPrefixListUi(Player player) {
		ArrayList<ElementButton> buttons = new ArrayList<ElementButton>();
		LinkedHashMap<String, LinkedHashMap<String, Integer>> target = this.loader.getRank(player).getPrefixList();

		// final int prefix_count = target.size();
		Object[] index_key = target.keySet().toArray();
		for (Object prefix : index_key) {
			buttons.add(new ElementButton((String) prefix));
		}
		FormWindowSimple newWindow = new FormWindowSimple("§8§l칭호 목록", "§l§f칭호 " + target.size() + "개", buttons);

		player.showFormWindow(newWindow, 1578);
	}

	/**
	 * @RESPOND
	 */

	@EventHandler
	public void onRespond(PlayerFormRespondedEvent e) {
		// long start = System.currentTimeMillis();
		Player player = e.getPlayer();
		int id = e.getFormID();
		if (e.getWindow() instanceof FormWindowSimple) {
			FormWindowSimple window = (FormWindowSimple) e.getWindow();
			if (window.getResponse() == null)
				return;
			final ElementButton button = window.getResponse().getClickedButton();
			if (id == 1577 && button != null) { // 메인
				String clickedButton = window.getResponse().getClickedButton().getText();
				if (clickedButton.equals("§l§8칭호 목록")) {
					showPrefixListUi(player);
				} /*
					 * else if(clickedButton.equals("§l§8칭호 변경")) {
					 * 
					 * }
					 */else if (clickedButton.equals("§l§8칭호 확인")) {
					sendShowPlayerPrefixs(player);

				} else if (clickedButton.equals("§l§8칭호 경매")) {

				} else if (clickedButton.equals("§l§8칭호 삭제")) {
					ArrayList<ElementButton> buttons = new ArrayList<ElementButton>();
					LinkedHashMap<String, LinkedHashMap<String, Integer>> target = this.loader.getRank(player)
							.getPrefixList();

					// final int prefix_count = target.size();
					Object[] index_key = target.keySet().toArray();
					for (Object prefix : index_key) {
						buttons.add(new ElementButton((String) prefix));
					}
					buttons.add(new ElementButton("§l§8돌아가기"));
					FormWindowSimple newWindow = new FormWindowSimple("§8§l칭호 삭제", "§l§c삭제§f할 칭호를 선택하세요.", buttons);
					this.plugin.getLogger().info(player.getName() + "님이 칭호삭제 조회");
					player.showFormWindow(newWindow, 1501);
				} else if (clickedButton.equals("§l§8칭호 추가(관리자)")) {
					ArrayList<Element> elements = new ArrayList<>();
					elements.add(new ElementInput("§f§l추가할 대상의 닉네임"));
					elements.add(new ElementInput("§f§l추가할 칭호"));
					FormWindowCustom changeNickForm = new FormWindowCustom("§8§l칭호 추가(관리자)", elements);
					player.showFormWindow(changeNickForm, 1030);
				} else if (clickedButton.equals("§l§8칭호 삭제(관리자)")) {
					ArrayList<Element> elements = new ArrayList<>();
					elements.add(new ElementInput("§f§l삭제할 대상의 닉네임"));
					FormWindowCustom changeNickForm = new FormWindowCustom("§8§l칭호 삭제(관리자)", elements);
					player.showFormWindow(changeNickForm, 1029);
				} else if (clickedButton.equals("§l§8닉네임 변경(관리자)")) {
					ArrayList<Element> elements = new ArrayList<>();
					elements.add(new ElementLabel(
							"§f§l1.닉네임이 너무 길진 않은지 확인하세요.\n2.닉네임이 적절한 내용인지 확인하세요.\n3.무단으로 유저의 닉네임을 변경하고있진 않은지 확인하세요.\n\n"));
					elements.add(new ElementInput("닉네임 입력칸(비워두면 본인)"));
					elements.add(new ElementInput("§l§f닉네임"));
					FormWindowCustom changeNickForm = new FormWindowCustom("§8§l닉네임 변경", elements);
					player.showFormWindow(changeNickForm, 1028);
				} else if (clickedButton.equals("§l§8칭호 변경(관리자)")) {
					ArrayList<Element> elements = new ArrayList<>();
					elements.add(new ElementInput("§f§l칭호를 변경할 대상의 닉네임"));
					FormWindowCustom changeNickForm = new FormWindowCustom("§8§l칭호 변경(관리자)", elements);
					player.showFormWindow(changeNickForm, 1031);
				} else if (clickedButton.equals("§l§8칭호 거래소")) {
					showSellingList(player);
					return;
				}
			} else if (id == 9927) {
				if (button.getText() == null)
					return;
				if (button.getText().equals(""))
					return;
				String seller = button.getText().split("판매자 §f: ")[1].split("\n")[0];
				if (this.provider.isSelling(seller) == false) {
					sendSimpleForm(player, "§l§8칭호 거래소", "§f§l해당 칭호는 방금 판매가 끝난 칭호입니다 ㅜㅜ");
					return;
				}
				LinkedHashMap<String, Object> data = this.provider.getSellingInfo(seller);
				ArrayList<ElementButton> buttons = new ArrayList<>();
				if(seller.equals(player.getName())) {
					buttons.add(new ElementButton("§c§l판매 중단"));
					buttons.add(new ElementButton("§8§l돌아가기"));
				}else {
				buttons.add(new ElementButton("§8§l구매할래요"));
				buttons.add(new ElementButton("§8§l돌아가기"));
				
				}String lefttime = "";
				int date = (int) data.get("date") - (int) (new Date().getTime() / 1000);
				if ((date / 3600) >= 1) {
					lefttime = "§6" + (int) Math.floor(date / 3600) + "§f시간 §6" + (int) Math.floor((date % 3600) / 60) + "§f분";
				} else {
					lefttime = "§c" + (int) Math.floor(date / 60) + "§f분";
				}
				FormWindowSimple aSimpleForm = new FormWindowSimple("§l§8칭호 거래소",
						"§6§l칭호 §f: §r" + data.get("prefix") + "\n§6§l판매가 §f: §g" + data.get("price") + "\n§6가치값 §f: §g"
								+ data.get("cost") + "\n§6남은 시간 §f: §g" + lefttime,
						buttons);
				this.selectedSeller.put(player.getName(), seller);
				player.showFormWindow(aSimpleForm,7755);
				return;
			}else if(id == 7755) {
				if(button.getText().equals("")) return;
				if(button.getText().equals("§8§l돌아가기")) {
					showSellingList(player);
					return;
				}
				if(button.getText().equals("§c§l판매 중단")) {
					this.provider.removeSeller(player.getName());
					sendSimpleForm(player, "§l§8칭호 거래소", "§f§l성공적으로 판매가 중단되었습니다.");
					return;
				}
				if(button.getText().equals("§8§l구매할래요") == false) return;
				String seller = this.selectedSeller.get(player.getName());
				if(seller == null) {
					sendSimpleForm(player, "§l§8칭호 거래소", "§l§f서버 오류가 발생하였습니다.");
				return;
				}
				if(this.provider.isSelling(seller) == false) {
					sendSimpleForm(player, "§l§8칭호 거래소", "§f§l해당 칭호는 판매가 끝난 칭호입니다.");
					return;
				}
				if(seller.equals(player.getName())) {
					sendSimpleForm(player, "§l§8칭호 거래소", "§f§l자신의 칭호는 구매할 수 없습니다.");
					return;
				}
				EconomyAPI economyAPI = this.listenerLoader.getEconomyAPI();
				LinkedHashMap<String, Object> data = this.provider.getSellingInfo(seller);
				if(economyAPI.myMoney(player) < (int)data.get("price")) {
					sendSimpleForm(player, "§l§8칭호 거래소", "§8§l돈이 §c부족§f합니다.\n판매가 : " + data.get("cost") + "\n보유 금액 : " + economyAPI.myMoney(player));
				return;
				}
				RankData rankData = this.loader.getRankByName(seller);
				String prefix = (String) data.get("prefix");
				if (!rankData.isExistPrefix(prefix)) {
					sendSimpleForm(player, "§l§8칭호 거래소", "§f판매자가 판매를 §6중단§f했습니다.");
					return;
				}
				if(rankData.getPrefixList().size() < 2) {
					sendSimpleForm(player, "§l§8칭호 거래소", "§f죄송합니다.\n해당 칭호를 일시적으로 구매할 수 없습니다.");
					return;
				}
				rankData.deletePrefixs(new String[] { prefix });
				if(rankData.getPrefix() != null) {
					if (rankData.getPrefix().equals(prefix)) {
						double random = Math.random();
						int randomID = (int) (random * (rankData.getPrefixList().size() - 1));
						if (rankData.isExistPrefixToIndex(randomID)) {
							rankData.setPrefix(rankData.getPrefixToIndex(randomID));
							this.provider.applyNameTag(seller);
						}
					}
				}
				RankData clientData = this.loader.getRankByName(player.getName());
				clientData.addPrefixs(new String[] { (String)data.get("prefix")}, (int)data.get("cost"));
				economyAPI.reduceMoney(player, (int) data.get("price"));
				economyAPI.addMoney(seller, (int) data.get("price"));
				Player sellerPlayer = this.plugin.getServer().getPlayerExact(seller);
				this.plugin.getServer().broadcastMessage("§l§6"+player.getName() + "§f님이 §6" + seller + "§f님의 칭호 §r" + data.get("prefix") + "§r§l§f를 구매하셨습니다!");
				if(sellerPlayer != null) {
					sellerPlayer.sendTitle("§f§l칭호 판매 §6성공§f!", "§f앗싸! §e" + data.get("price")+"§f원 얻었다!", 20, 40, 20);
				}else {
					plugin.addMail(seller, "§l§f[ 칭호 ] §d" + player.getName() + "§f님이 §r" + (String)data.get("prefix") + "§r§l를 §d " + data.get("price") + "§f원에 구매하셨습니다.");
				}
				this.plugin.showBossbar("§d§l" + player.getName() + "§f님이 §d" + seller + "§f님의 칭호 §r" + (String)data.get("prefix") + "§r§l를 구매하였습니다.");
				this.provider.removeSeller(seller);
				sendSimpleForm(player, "§l§8칭호 거래소","§f§l성공적으로 §r" + data.get("prefix") + " §r§f§l를 구매하셨습니다.");
			}			
			else if (id == 1578) {
				String buttonString = window.getResponse().getClickedButton().getText();
				if (buttonString == null)
					return;
				if (buttonString.trim().equals(""))
					return;
				String perchaseDate = "";
				int costs = 1;
				LinkedHashMap<String, LinkedHashMap<String, Integer>> target = this.loader.getRank(player)
						.getPrefixList();
				Object[] index_key = target.keySet().toArray();
				for (Object prefix : index_key) {
					if (!((String) prefix).equals(buttonString))
						continue;
					costs = ((LinkedHashMap<String, Integer>) target.get((String) prefix)).get("costs");
					perchaseDate = sdf
							.format(((LinkedHashMap<String, Integer>) target.get((String) prefix)).get("date") * 1000L);
					break;
				}
				ArrayList<ElementButton> buttons = new ArrayList<>();
				buttons.add(new ElementButton("§8§l칭호로 설정하기"));
				buttons.add(new ElementButton("§8§l판매하기"));
				buttons.add(new ElementButton("§8§l돌아가기"));
				FormWindowSimple prefixInfoWindow = new FormWindowSimple("§8§l칭호 정보", "§g§l칭호§f : §r" + buttonString
						+ "\n§r§l§g얻은 날짜§f : §e" + perchaseDate + "\n§g가치 §f: §e" + numberToKorean(costs) + "원\n\n",
						buttons);
				this.uiPrefixSet.put(player.getName(), buttonString);
				this.plugin.getLogger().info(player.getName() + "님이 칭호 : " + buttonString + "의 정보를 조회하셨습니다.");
				player.showFormWindow(prefixInfoWindow, 1890);
			} else if (id == 1890) {
				String playerName = player.getName();
				String buttonString = window.getResponse().getClickedButton().getText();
				if (buttonString == null)
					return;
				if (buttonString.trim().equals(""))
					return;
				if (buttonString.equals("§8§l칭호로 설정하기")) {
					// Object[] index_key = this.loader.getRank ( player ).getPrefixList
					// ().keySet().toArray();
					String selectedprefixString = this.uiPrefixSet.get(playerName);
					if (selectedprefixString == null) {
						this.plugin.message(player, "오류가 발생하였습니다 : §g17");
						this.plugin.getLogger().error(playerName + "님의 칭호 설정 요청이 오류로 인해 거부되었습니다.");
						return;
					}
					/*
					 * for(Object prefix : index_key) { if(! ((String)
					 * prefix).equals(selectedprefixString)) continue;
					 * 
					 * break; }
					 */
					RankData rankData = this.loader.getRank((Player) player);
					rankData.setPrefix(selectedprefixString);
					this.provider.applyNameTag(playerName);
					player.showFormWindow(new FormWindowSimple("§8§l칭호 설정",
							"§l§g성공적으로 §f칭호가 §r" + selectedprefixString + "§r§f§l으로 설정되었습니다!"));
					this.plugin.getLogger().info(playerName + "님의 칭호가 " + selectedprefixString + "으로 설정되었습니다.");
					this.uiPrefixSet.remove(playerName);
				} else if (buttonString.equals("§8§l판매하기")) {
					ArrayList<Element> elements = new ArrayList<>();
					LinkedHashMap<String, LinkedHashMap<String, Integer>> rd = this.provider.getRank(player)
							.getPrefixList();
					int cost = ((LinkedHashMap<String, Integer>) rd.get((String) this.uiPrefixSet.get(playerName)))
							.get("costs");
					this.uiSaveCostForSelling.put(playerName, cost);
					elements.add(new ElementLabel("§f§l정말 " + this.uiPrefixSet.get(playerName)
							+ " §r§l 를 판매하실건가요?\n해당 칭호의 가치는 §g" + numberToKorean(cost) + "§f원 입니다.\n해당 칭호는 §g" + ((cost / 2) > 0 ? (cost /2) : 1)
							+ " ~ 500백만원§f의 가격으로 판매할 수 있습니다."));
					elements.add(new ElementInput("", "판매가"));
					FormWindowCustom cutomForm = new FormWindowCustom("§8§l칭호 판매", elements);
					player.showFormWindow(cutomForm, 4900);
				} else {
					showPrefixListUi(player);
				}
			} else if (id == 15099) { // 칭호 확인 플레이어 이름 없음
				ElementButton RPbutton = window.getResponse().getClickedButton();
				if (RPbutton != null) {
					if (RPbutton.getText() == "§8§l돌아가기") {
						sendShowPlayerPrefixs(player);
					} else if (RPbutton.getText() == "§8§l메인으로") {
						sendMainUI(player);
					}
				}
			} else if (id == 1530) { // 다른 사람 칭호 목록
				String targetPlayer = this.uiSeeOtherPlayersPrefix.get(player.getName());
				if (targetPlayer == null) {
					this.plugin.message(player, "오류가 발생하였습니다 : §g16");
					this.plugin.getLogger().error(player.getName() + "님의 칭호 확인이 오류로 인해 거부되었습니다.");
					return;
				}

				String buttonString = window.getResponse().getClickedButton().getText();
				if (buttonString == null)
					return;
				if (buttonString.trim().equals(""))
					return;
				String perchaseDate = "";
				int costs = 1;
				LinkedHashMap<String, LinkedHashMap<String, Integer>> target = this.loader.getRankByName(targetPlayer)
						.getPrefixList();
				Object[] index_key = target.keySet().toArray();
				for (Object prefix : index_key) {
					if (!((String) prefix).equals(buttonString))
						continue;
					costs = ((LinkedHashMap<String, Integer>) target.get((String) prefix)).get("costs");
					perchaseDate = sdf
							.format(((LinkedHashMap<String, Integer>) target.get((String) prefix)).get("date") * 1000L);
					break;
				}
				ArrayList<ElementButton> buttons = new ArrayList<>();
				buttons.add(new ElementButton("§8§l메인으로"));
				FormWindowSimple prefixInfoWindow = new FormWindowSimple("§8§l§o" + targetPlayer + "§r§o§8의 칭호 정보",
						"§g§l칭호§f : §r" + buttonString + "\n§r§l§g얻은 날짜§f : §e" + perchaseDate + "\n§g가치 §f: §e"
								+ numberToKorean(costs) + "원\n\n",
						buttons);
				player.showFormWindow(prefixInfoWindow, 15922);
			} else if (id == 15922) {
				if (window.getResponse().getClickedButton() != null) {
					sendMainUI(player);
				}
			} else if (id == 1501) {
				if (window.getResponse().getClickedButton() == null)
					return;
				String clickedButtonText = window.getResponse().getClickedButton().getText();
				if (clickedButtonText == null)
					return;
				if (clickedButtonText.equals("§l§8돌아가기")) {
					sendMainUI(player);
					return;
				}
				this.uiDeletePrefix.put(player.getName(), clickedButtonText);
				FormWindowModal modalForm = new FormWindowModal("§8§l칭호 삭제",
						"§l§f정말 " + clickedButtonText + "§r§l§f칭호를 §c삭제§f하실건가요?\n§c이 작업은 되돌릴 수 없습니다!", "네", "아니오");
				player.showFormWindow(modalForm, 1922);
			} else if (id == 1101) {
				String tartgetNick = this.uiAdminDeletePrefix.get(player.getName());
				if (tartgetNick == null)
					return;
				String clickedButtonString = button.getText();
				if (clickedButtonString == null)
					return;
				if (clickedButtonString.trim().equals(""))
					return;
				if (clickedButtonString.equals("§l§c취소하기")) {
					sendMainUI(player);
					return;
				}
				FormWindowModal aModal = new FormWindowModal("§8§l칭호 삭제",
						"§l§f정말 §6" + tartgetNick + "§f님의 " + clickedButtonString
								+ "§r§f§l을 §c삭제§f하시겠어요?\n§c복구할 수 없어요! 물론 다시 추가하면 되겠지만 date값과 가치값은 되돌릴 수 없습니다!",
						"네", "아니오");
				this.uiAdminDeleteSelPrefix.put(player.getName(), clickedButtonString);
				player.showFormWindow(aModal, 1937);
				return;
			} else if (id == 1325) {
				String targetName = this.uiAdminChangePrefix.get(player.getName());
				if (targetName == null)
					return;
				String targetPrefix = button.getText();
				if (targetPrefix == "§c§l취소하기") {
					sendMainUI(player);
					return;
				}
				RankData rankData = this.loader.getRankByName(targetName);
				if (!rankData.isExistPrefix(targetPrefix)) {
					sendSimpleForm(player, "§8§l칭호 추가 (관리자)", "§f§l오류가 발생했어요.\n존재하지 않는 칭호입니다.");
					return;
				}
				rankData.setPrefix(targetPrefix);
				this.provider.applyNameTag(targetName);
				sendSimpleForm(player, "§8§l칭호 추가 (관리자)",
						"§f§l성공적으로 §6" + targetName + "§f의 칭호를" + targetPrefix + "§r§f§l으로 설정했어요.");
				return;
			}

		} else if (e.getWindow() instanceof FormWindowCustom) { ////////////////////////////////////
			// long end = System.currentTimeMillis();
			// this.plugin.getLogger().info((end-start) + "");
			FormWindowCustom window = (FormWindowCustom) e.getWindow();
			if (window.getResponse() == null)
				return;
			if (id == 1579) {
				// plugin.getLogger().info(window.getResponse().getResponse(1) + " 광" +
				// window.getResponse().getResponse(0) + " 영" +
				// window.getResponse().getInputResponse(1) + "광" +
				// window.getResponse().getInputResponse(0) + "영");
				if (window.getResponse() == null)
					return;
				String responsedInput = window.getResponse().getInputResponse(1);
				if (responsedInput == null) {
					ArrayList<ElementButton> buttons = new ArrayList<>();
					buttons.add(new ElementButton("§8§l돌아가기"));
					buttons.add(new ElementButton("§8§l메인으로"));
					FormWindowSimple plzEnterName = new FormWindowSimple("§8§l칭호 확인", "§f§l플레이어 이름을 입력해주세요.", buttons);
					player.showFormWindow(plzEnterName, 15099);
				} else if (responsedInput.trim().equals("")) {
					ArrayList<ElementButton> buttons = new ArrayList<>();
					buttons.add(new ElementButton("§8§l돌아가기"));
					buttons.add(new ElementButton("§8§l메인으로"));
					FormWindowSimple plzEnterName = new FormWindowSimple("§8§l칭호 확인", "§f§l플레이어 이름을 입력해주세요.", buttons);
					player.showFormWindow(plzEnterName, 15099);
				} else {
					responsedInput = responsedInput.trim();
					Player player2 = (Player) this.plugin.getServer().getPlayer(responsedInput);
					if (player2 == null) {
						RankData rankData = this.loader.getRankByName(responsedInput);
						if (rankData.getPrefixList().size() > 0) {
							ArrayList<ElementButton> buttons = new ArrayList<ElementButton>();
							rankData.getPrefixList().forEach((prefix, bool) -> {
								buttons.add(new ElementButton((String) prefix));
							});
							this.plugin.getLogger().info(player.getName() + ",0,칭호확인," + responsedInput);
							this.uiSeeOtherPlayersPrefix.put(player.getName(), responsedInput);
							FormWindowSimple newWindow = new FormWindowSimple("§8§l" + responsedInput + "의 칭호 목록",
									"§l§f칭호 " + rankData.getPrefixList().size() + "개", buttons);
							player.showFormWindow(newWindow, 1530);
						} else {
							ArrayList<ElementButton> buttons = new ArrayList<>();
							buttons.add(new ElementButton("§8§l돌아가기"));
							buttons.add(new ElementButton("§8§l메인으로"));
							FormWindowSimple plzEnterName = new FormWindowSimple("§8§l칭호 확인", "§f§l해당 플레이어를 찾을 수 없습니다.",
									buttons);
							player.showFormWindow(plzEnterName, 15099);
						}
						// Player player2 = (Player)
						// this.plugin.getServer().getPlayerExact(responsedInput);
						return;
					}
					ArrayList<ElementButton> buttons = new ArrayList<ElementButton>();
					LinkedHashMap<String, LinkedHashMap<String, Integer>> target = this.loader.getRank(player2)
							.getPrefixList();

					// final int prefix_count = target.size();
					Object[] index_key = target.keySet().toArray();
					for (Object prefix : index_key) {
						buttons.add(new ElementButton((String) prefix));
					}
					this.plugin.getLogger().info(player.getName() + ",1,칭호확인," + player2.getName());
					this.uiSeeOtherPlayersPrefix.put(player.getName(), player2.getName());
					FormWindowSimple newWindow = new FormWindowSimple("§l§8" + player2.getName() + "의 칭호 목록",
							"§l§f칭호 " + target.size() + "개", buttons);
					player.showFormWindow(newWindow, 1530);

				}
			} else if (id == 1028) { // 닉네임
				if (player.isOp() == false)
					return;
				String targetnick = (String) window.getResponse().getResponse(1);
				String nick = (String) window.getResponse().getResponse(2);
				this.plugin.getLogger().info(player.getName() + "의 닉네임 변경 요청 : " + targetnick + " , " + nick);
				if (nick == null)
					return;
				if (nick.trim().equals("")) {
					FormWindowSimple simpeForm = new FormWindowSimple("§8§l닉네임 변경(관리자)", "변경할 닉네임칸이 비어있어요");
					player.showFormWindow(simpeForm);
					return;
				}
				if (targetnick == null) {
					targetnick = player.getName();
				} else if (targetnick.trim().equals("")) {
					targetnick = player.getName();
				}

				this.plugin.korea(targetnick, nick);
				FormWindowSimple simpleForm = new FormWindowSimple("§8§l닉네임 변경", "§f§6" + targetnick + "§f님의 닉네임이 "
						+ nick + "으로 변경되었어요. \n 참고로 유효하지 않은 닉네임인지 확인해 주셔야 해요.\n유효하지 않은 플레이어의 이름이라도 변경되거든요!");
				player.showFormWindow(simpleForm);
				return;
			} else if (id == 1029) { // 삭제
				if (player.isOp() == false)
					return;
				String targetNick = window.getResponse().getInputResponse(0);
				this.plugin.getLogger().info(player.getName() + "의 관리자 칭호 삭제 요청, target:" + targetNick);
				if (targetNick == null)
					return;
				if (targetNick.trim().equals("")) {
					FormWindowSimple simpeForm = new FormWindowSimple("§8§l칭호 삭제(관리자)", "플레이어 이름칸이 비어있어요");
					player.showFormWindow(simpeForm);
					return;
				}
				ArrayList<ElementButton> buttons = new ArrayList<ElementButton>();
				LinkedHashMap<String, LinkedHashMap<String, Integer>> target = this.loader.getRankByName(targetNick)
						.getPrefixList();
				Object[] index_key = target.keySet().toArray();
				if (index_key.length < 1) {
					FormWindowSimple simpeForm = new FormWindowSimple("§8§l칭호 삭제(관리자)",
							"§f§l해당 유저는 칭호를 가지고 있지 않아요.\n플레이어 이름을 제대로 입력했는지 확인해주세요.\n입력된 플레이어 : " + targetNick);
					player.showFormWindow(simpeForm);
					return;
				}
				for (Object prefix : index_key) {
					buttons.add(new ElementButton((String) prefix));
				}
				buttons.add(new ElementButton("§l§c취소하기"));
				FormWindowSimple newWindow = new FormWindowSimple("§8§l칭호 삭제",
						"§l§c삭제§f할 칭호를 선택하세요.\n대상 플레이어 : " + targetNick, buttons);
				this.plugin.getLogger().info(player.getName() + "님이 " + targetNick + "의 칭호삭제 조회");
				this.uiAdminDeletePrefix.put(player.getName(), targetNick);
				player.showFormWindow(newWindow, 1101);
			} else if (id == 1030) { // 추가
				if (player.isOp() == false)
					return;
				String targetNick = window.getResponse().getInputResponse(0);
				String targetPrefix = window.getResponse().getInputResponse(1);
				if (targetNick == null || targetPrefix == null)
					return;
				if (targetNick.trim().equals("") || targetPrefix.trim().equals("")) {
					sendSimpleForm(player, "§8§l칭호 추가 (관리자)", "§f§l닉네임 또는 칭호칸이 비어있습니다.");
					return;
				}
				RankData rankData = this.loader.getRankByName(targetNick);
				if (rankData.isExistPrefix(targetPrefix)) {
					sendSimpleForm(player, "§8§l칭호 추가 (관리자)", "§f§l이미 존재하는 칭호입니다.");
					return;
				}
				rankData.addPrefixs(new String[] { targetPrefix.trim() }, 1);
				sendSimpleForm(player, "§8§l칭호 추가 (관리자)",
						"§6§l성공§f적으로 §6 " + targetNick + "§f님에게 " + targetPrefix + "§r§f를 추가하였습니다");

				this.plugin.getLogger().info(player.getName() + "에 의해 " + targetNick + "에" + targetPrefix + "가 추가됨");

				return;
			} else if (id == 1031) {
				if (player.isOp() == false)
					return;
				String targetNick = window.getResponse().getInputResponse(0);
				if (targetNick == null)
					return;
				if (targetNick.trim().equals("")) {
					sendSimpleForm(player, "§8§l칭호 추가 (관리자)", "§f§l플레이어 닉네임 칸이 비어있습니다.");
					return;
				}
				RankData rankData = this.loader.getRankByName(targetNick);
				if (rankData.getPrefixList().size() > 0) {
					ArrayList<ElementButton> buttons = new ArrayList<ElementButton>();
					rankData.getPrefixList().forEach((prefix, bool) -> {
						buttons.add(new ElementButton((String) prefix));
					});
					buttons.add(new ElementButton("§c§l취소하기"));
					this.plugin.getLogger().info(player.getName() + ",0,칭호확인," + targetNick);
					this.uiAdminChangePrefix.put(player.getName(), targetNick);
					FormWindowSimple newWindow = new FormWindowSimple("§8§l" + targetNick + "의 칭호 목록",
							"§l§f칭호 " + rankData.getPrefixList().size() + "개", buttons);
					player.showFormWindow(newWindow, 1325);

				} else {
					sendSimpleForm(player, "§8§l칭호 추가 (관리자)", "§f§l" + targetNick + "는 존재하지 않는 플레이어입니다.");
					return;
				}

			} else if (id == 4900) {
				String respond = window.getResponse().getInputResponse(1);
				if (respond.trim().equals("")) {
					sendSimpleForm(player, "§8§l칭호 판매", "§f§l판매가가 비어있어 칭호를 판매할 수 없습니다 !");
					return;
				}
				this.plugin.getLogger().info(player.getName() + "님이" + respond + "원으로 판매 요청");
				if (isNumeric(respond) == false) {
					sendSimpleForm(player, "§8§l칭호 판매", "§f§l판매가는 무조건 §6숫자§f로 이루어져 있어야 합니다 !\n §c판매 불가능 !");
					return;
				}
				int SellPrice = Integer.parseInt(respond);
				int cost = this.uiSaveCostForSelling.get(player.getName());
				if (SellPrice > 0 && SellPrice > (cost / 2) && SellPrice < 5000000) {
					if (this.provider.isSelling(player.getName()) == true) {
						sendSimpleForm(player, "§8§l칭호 판매",
								"§f§l이미 칭호를 판매하고 계십니다.\n칭호는 한번에 최대 §61개§f까지 판매 가능하며, 추가로 판매하려면 §624시간§f을 기다리거나 기존 칭호의 판매를 중단해주세요.");
						return;
					}
					this.provider.addSeller(player.getName(), cost, SellPrice, this.uiPrefixSet.get(player.getName()));
					sendSimpleForm(player, "§8§l칭호 판매",
							"§l§a성공§f적으로 §6판매등록§f을 완료하였습니다.\n§6등록된 칭호 §f : " + this.uiPrefixSet.get(player.getName()));
					this.plugin.showBossbar("§l§d" + player.getName() + "§f님이 칭호 " + this.uiPrefixSet.get(player.getName()) + "§r§l§f를 §d" + SellPrice + "§f원에 판매중입니다.");
					return;
				} else {
					sendSimpleForm(player, "§8§l칭호 판매",
							"§l§6판매가§f가 §6" +( (cost / 2) > 0 ? (cost/2) : 1)+ "§f보다 작거나 §65000000§f보다 큽니다.\n§6판매 불가능 !");
					return;
				}
			}
		} else if (e.getWindow() instanceof FormWindowModal) {
			FormWindowModal window = (FormWindowModal) e.getWindow();
			if (window.getResponse() == null)
				return;
			int clickedButton = window.getResponse().getClickedButtonId();
			if (id == 1922) {
				String selectedPrefix = this.uiDeletePrefix.get(player.getName());
				if (selectedPrefix == null) {
					this.plugin.message(player, "§l§f오류가 발생하였습니다. 201");
					this.plugin.getLogger().error(player.getName() + "님의 칭호 삭제 요청 오류로 인해 취소됨");
					return;
				}
				if (clickedButton == 1) {
					this.plugin.getLogger().info(player.getName() + "님의 칭호 삭제 요청 취소됨");
					sendMainUI(player);
				} else if (clickedButton == 0) {
					RankData rankData = this.provider.getRankByName(player.getName());
					if (rankData.isExistPrefix(selectedPrefix) == false) {
						this.plugin.message(player, "§l§f오류가 발생하였습니다. 존재하지 않는 칭호입니다!");
						this.plugin.getLogger().error(player.getName() + "님의 유효하지 않는 칭호 삭제 요청 : " + selectedPrefix);
						return;
					}
					rankData.deletePrefixs(new String[] { selectedPrefix });
					if (rankData.getPrefix().equals(selectedPrefix)) {
						double random = Math.random();
						int randomID = (int) (random * (rankData.getPrefixList().size() - 1));
						if (rankData.isExistPrefixToIndex(randomID)) {
							rankData.setPrefix(rankData.getPrefixToIndex(randomID));
							this.provider.applyNameTag(player.getName());
						}
					}
					FormWindowSimple aFormWindowSimple = new FormWindowSimple("§8§l칭호 삭제",
							"§l§f성공적으로 " + selectedPrefix + "§r칭호가 §6삭제§f되었습니다.");
					this.uiDeletePrefix.remove(player.getName());
					player.showFormWindow(aFormWindowSimple);
					this.plugin.getLogger().info(player.getName() + "의 " + selectedPrefix + "§f삭제요청 성공");
				}
			} else if (id == 1937) {
				if (clickedButton == 1) {
					sendMainUI(player);
					return;
				}
				String tartget = this.uiAdminDeletePrefix.get(player.getName());
				String tartgetPrefix = this.uiAdminDeleteSelPrefix.get(player.getName());
				if (tartget == null)
					return;
				if (tartgetPrefix == null)
					return;
				RankData rankData = this.provider.getRankByName(tartget);
				if (rankData.isExistPrefix(tartgetPrefix) == false) {
					this.plugin.message(player, "§l§f오류가 발생하였습니다. 존재하지 않는 칭호입니다!");
					this.plugin.getLogger().error(player.getName() + "님의 유효하지 않는 칭호 삭제 요청 (관리자) : " + tartgetPrefix);
					return;
				}
				rankData.deletePrefixs(new String[] { tartgetPrefix });
				if (rankData.getPrefix().equals(tartgetPrefix)) {
					if (rankData.isExistPrefixToIndex(0)) {
						rankData.setPrefix(rankData.getPrefixToIndex(0));
						this.provider.applyNameTag(tartget);
					}
				}
				FormWindowSimple aFormWindowSimple = new FormWindowSimple("§8§l칭호 삭제",
						"§l§f성공적으로 §6" + tartget + " §f님의" + tartgetPrefix + "§r칭호가 §6삭제§f되었습니다.");
				this.uiDeletePrefix.remove(player.getName());
				this.uiAdminDeletePrefix.remove(player.getName());
				this.uiAdminDeleteSelPrefix.remove(player.getName());
				player.showFormWindow(aFormWindowSimple);
				this.plugin.getLogger().info(player.getName() + "의 " + tartgetPrefix + "§f삭제요청 성공");

			}
		}
	}

	public void sendHelpMessages(Player player) {
		this.plugin.message((Player) player, this.plugin.get("rank-help1"));
		this.plugin.message((Player) player, this.plugin.get("rank-help2"));
		this.plugin.message((Player) player, this.plugin.get("rank-help3"));
		this.plugin.message((Player) player, this.plugin.get("rank-help6"));
		if (player.hasPermission("rankmanager.rank.control")) {
			this.plugin.message((Player) player, this.plugin.get("rank-help4"));
			this.plugin.message((Player) player, this.plugin.get("rank-help5"));
			this.plugin.message((Player) player, "§6칭호 닉네임 [닉네임] [이름] §f - 해당유저의 닉네임을 변경합니다.");
		}
	}

	public boolean onCommand(CommandSender player, Command command, String label, String[] args) {
		if(player instanceof Player == false) {
			player.sendMessage("인게임에서 입력해주세요.");
		}
		if (!player.hasPermission("rankmanager.rank.manage"))
			return false;
		if (command.getName().toLowerCase() != this.plugin.get("rank"))
			return false;

		if (args.length == 0) {
			if (!(player instanceof Player)) {
				sendHelpMessages((Player) player);
				return true;
			}
			sendMainUI((Player) player);
			return false;
		}

		String string = args[0];
		if ("?".equals(string)) {
			sendHelpMessages((Player) player);
			return true;
		} else if ("닉네임".equals(string)) {
			if (!player.hasPermission("rankmanager.rank.control"))
				return false;
			if (args.length < 3) {
				this.plugin.message((Player) player, "§6칭호 닉네임 <닉네임> <이름> §f - 해당유저의 닉네임을 변경합니다.");
				return false;
			}
			this.plugin.korea(args[1], args[2]);
			this.plugin.message((Player) player, args[1] + " 님의 닉네임이 " + args[2] + "로 설정 되었습니다.");
		} else if (string.equals("목록")) {
			int index;
			if (args.length > 1) {
				index = isNumeric(args[1]) ? Integer.parseInt(args[1]) : 1;
			} else {
				index = 1;
			}
			this.getPrefixList((Player) player, index);
		} else if (string.equals("변경")) {
			if (!(args.length > 1)) {
				this.plugin.message((Player) player, this.plugin.get("rank-help2"));
				return true;
			}
			if (isNumeric(args[1]) == false) {
				this.plugin.message((Player) player, this.plugin.get("rank-help2"));
				return true;
			}
			RankData rankData = this.loader.getRank((Player) player);
			if (!rankData.isExistPrefixToIndex(Integer.parseInt(args[1]))) {
				this.plugin.alert((Player) player, this.plugin.get("not-exist-that-prefix"));
				return true;
			}
			rankData.setPrefix(rankData.getPrefixToIndex(Integer.parseInt(args[1])));
			this.provider.applyNameTag(player.getName());
			this.plugin.message((Player) player, this.plugin.get("prefix-changed"));
		} else if (string.equals("추가")) {
			if (!player.hasPermission("rankmanager.rank.control"))
				return false;
			if (!(args.length > 1)) {
				this.plugin.message((Player) player, this.plugin.get("rank-help4"));
				return true;
			}
			if (!(args.length > 2)) {
				RankData rankData = this.loader.getRank((Player) player);
				if (rankData.isExistPrefix(args[1])) {
					this.plugin.alert((Player) player, this.plugin.get("already-exist-that-prefix"));
					return true;
				}
				rankData.addPrefixs(new String[] { args[1] }, 1);
				this.plugin.message((Player) player, this.plugin.get("prefix-added"));
			} else {
				RankData rankData = this.loader.getRankByName(args[1]);
				if (rankData.isExistPrefix(args[2])) {
					this.plugin.alert((Player) player, this.plugin.get("already-exist-that-prefix"));
					return true;
				}
				rankData.addPrefixs(new String[] { args[2] }, 1);
				this.plugin.message((Player) player, this.plugin.get("prefix-added"));
			}
		} else if (string.equals("삭제")) {
			if (!(args.length > 1)) {
				this.plugin.message((Player) player, this.plugin.get("rank-help3"));
				return true;
			}
			if (!(args.length > 2)) {
				if (isNumeric(args[1]) == false) {
					this.plugin.message((Player) player, this.plugin.get("rank-help3"));
					return true;
				}
				RankData rankData = this.loader.getRank((Player) player);
				if (!rankData.isExistPrefixToIndex(Integer.parseInt(args[1]))) {
					this.plugin.alert((Player) player, this.plugin.get("not-exist-that-prefix"));
					return true;
				}
				String selectedPrefix = rankData.getPrefixToIndex(Integer.parseInt(args[1]));
				rankData.deletePrefixs(new String[] { rankData.getPrefixToIndex(Integer.parseInt(args[1])) });
				if (rankData.getPrefix().equals(selectedPrefix)) {
					double random = Math.random();
					int randomID = (int) (random * (rankData.getPrefixList().size() - 1));
					if (rankData.isExistPrefixToIndex(randomID)) {
						rankData.setPrefix(rankData.getPrefixToIndex(randomID));
						this.provider.applyNameTag(player.getName());
					}
				}
				this.plugin.message((Player) player, this.plugin.get("prefix-deleted"));
			} else {
				if (!player.hasPermission("rankmanager.rank.control"))
					return false;
				if (isNumeric(args[2]) == false) {
					this.plugin.message((Player) player, this.plugin.get("rank-help3"));
					return true;
				}
				RankData rankData = this.loader.getRankByName(args[1]);
				if (!rankData.isExistPrefixToIndex(Integer.parseInt(args[2]))) {
					this.plugin.alert((Player) player, this.plugin.get("not-exist-that-prefix"));
					return true;
				}
				String selectedPrefix = rankData.getPrefixToIndex(Integer.parseInt(args[2]));
				rankData.deletePrefixs(new String[] { rankData.getPrefixToIndex(Integer.parseInt(args[2])) });
				if (rankData.getPrefix().equals(selectedPrefix)) {
					double random = Math.random();
					int randomID = (int) (random * (rankData.getPrefixList().size() - 1));
					if (rankData.isExistPrefixToIndex(randomID)) {
						rankData.setPrefix(rankData.getPrefixToIndex(randomID));
						this.provider.applyNameTag(args[1]);
					}
				}
				this.plugin.message((Player) player, this.plugin.get("prefix-deleted"));
			}
		} else if (string.equals("확인")) {
			if (!(args.length > 1)) {
				this.plugin.message((Player) player, this.plugin.get("rank-help6"));
				return true;
			}
			RankData rankData = this.loader.getRankByName(args[1]);
			stringz = TextFormat.DARK_AQUA + "";
			rankData.getPrefixList().forEach((prefix, bool) -> {
				stringz += "<{prefix}> ";
			});
			this.plugin.message((Player) player, this.plugin.get("show-the-user-prefix-list"));
			this.plugin.message((Player) player, stringz);
		} else {
			sendHelpMessages((Player) player);
			return true;
		}
		return true;
	}

	public void getPrefixList(Player player, int index) {
		// if(index ==(Integer) null) index = 1;

		int once_print = 10;

		LinkedHashMap<String, LinkedHashMap<String, Integer>> target = this.loader.getRank(player).getPrefixList();

		int index_count = target.size();
		Object[] index_key = target.keySet().toArray();
		int full_index = (int) Math.floor(index_count / once_print);
		if (index_count > full_index * once_print)
			full_index++;

		if (index <= full_index) {
			player.sendMessage(TextFormat.WHITE + this.plugin.get("now-list-show") + " (" + index + "/" + full_index
					+ ") " + this.plugin.get("index_count") + " : " + index_count + "개");
			player.sendMessage(TextFormat.WHITE + this.plugin.get("you-can-change-prefix"));
			String message = "";
			for (int for_i = once_print; for_i >= 1; for_i--) {
				int now_index = index * once_print - for_i;
				// System.out.println(index_key);
				// System.err.println("위아 now index = " + now_index);
				if (index_key.length <= now_index)
					break;
				String now_key = (String) index_key[now_index];
				message += TextFormat.WHITE + "[" + now_index + "] : " + TextFormat.RESET + now_key + "\n";
			}
			player.sendMessage(message);
		} else {
			player.sendMessage(TextFormat.RED + this.plugin.get("there-is-no-list"));
			return;
		}
	}
}
