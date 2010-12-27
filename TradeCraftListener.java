class TradeCraftListener extends PluginListener {

    private final TradeCraft plugin;

    public TradeCraftListener(TradeCraft plugin) {
        this.plugin = plugin;
    }

    public void onBlockRightClicked(Player player, Block blockClicked, Item itemInHand) {
        TradeCraftStore store = plugin.getStoreFromSignBlock(blockClicked);

        if (store == null) {
            return;
        }

        store.handleRightClick(player);
    }

    public boolean onBlockBreak(Player player, Block block) {
        TradeCraftStore store = plugin.getStoreFromSignOrChestBlock(block);

        if (store == null) {
            return false;
        }

        if (store.playerCanDestroy(player)) {
            return false;
        }

        plugin.sendMessage(player, "You can't destroy this sign or chest!");

        return true;
    }

    public boolean onSignChange(Player player, Sign sign) {
        String ownerName = plugin.getOwnerName(sign);

        if (ownerName == null) {
            return false;
        }

        if (ownerName.equals(player.getName())) {
            return false;
        }

        // The sign has some other player's name on it.
        // Don't let the current player create it.
        plugin.sendMessage(player, "You can't create signs with other players names on them!");

        return true;
    }

}
