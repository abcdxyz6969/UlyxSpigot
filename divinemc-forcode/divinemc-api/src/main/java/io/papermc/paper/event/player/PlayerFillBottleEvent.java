package io.papermc.paper.event.player;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class PlayerFillBottleEvent extends PlayerEvent implements Cancellable {
	private static final HandlerList HANDLER_LIST = new HandlerList();

	private final EquipmentSlot hand;
	private final ItemStack bottle;
	private ItemStack resultItem;
	private boolean cancelled;

	public PlayerFillBottleEvent(Player player, EquipmentSlot hand, ItemStack bottle, ItemStack resultItem) {
		super(player);
		this.hand = hand;
		this.bottle = bottle;
		this.resultItem = resultItem;
	}

	/**
	 * The hand used to fill the bottle.
	 *
	 * @return the hand
	 */
	public EquipmentSlot getHand() {
		return this.hand;
	}

	/**
	 * Gets the bottle item that's being filled.
	 *
	 * @return the bottle item
	 */
	public ItemStack getBottle() {
		return this.bottle;
	}

	/**
	 * Gets the result of the bottle that's being filled.
	 *
	 * @return the result of the filling
	 */
	public ItemStack getResultItem() {
		return this.resultItem;
	}

	/**
	 * Sets the result of the bottle being filled.
	 *
	 * @param resultItem the result of the filling
	 */
	public void setResultItem(ItemStack resultItem) {
		this.resultItem = resultItem;
	}

	@Override
	public boolean isCancelled() {
		return this.cancelled;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Cancelling this event will prevent {@link #getBottle()} from being
	 * replaced/consumed.
	 */
	@Override
	public void setCancelled(boolean cancel) {
		this.cancelled = cancel;
	}

	@Override
	public HandlerList getHandlers() {
		return HANDLER_LIST;
	}

	public static HandlerList getHandlerList() {
		return HANDLER_LIST;
	}
}
