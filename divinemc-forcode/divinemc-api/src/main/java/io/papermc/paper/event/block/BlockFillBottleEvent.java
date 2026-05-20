package io.papermc.paper.event.block;

import org.bukkit.block.Block;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

/**
 * Called when a {@link org.bukkit.block.Dispenser} fills up a bottle.
 */
@NullMarked
public class BlockFillBottleEvent extends BlockEvent implements Cancellable {
	private static final HandlerList HANDLER_LIST = new HandlerList();

	private final ItemStack bottle;
	private ItemStack resultItem;
	private boolean cancelled;

	public BlockFillBottleEvent(Block block, ItemStack bottle, ItemStack resultItem) {
		super(block);
		this.bottle = bottle;
		this.resultItem = resultItem;
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
	 * Gets the result of the glass bottle that's being filled.
	 *
	 * @return the result of the filling
	 */
	public ItemStack getResultItem() {
		return this.resultItem;
	}

	/**
	 * Sets the result of the glass bottle being filled.
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
