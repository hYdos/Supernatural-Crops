package com.fusionflux.supernaturalcrops.mixin;

import com.fusionflux.supernaturalcrops.blocks.SupernaturalCropsScrapedStone;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.Set;

@Mixin(HoeItem.class)
public class HoeItemMixin extends MiningToolItem {

	private static final Map<Block, BlockState> SCRAPED_BLOCKS;

	protected HoeItemMixin(float attackDamage, float attackSpeed, ToolMaterial material, Set<Block> effectiveBlocks, Settings settings) {
		super(attackDamage, attackSpeed, material, effectiveBlocks, settings);
	}

	@Inject(method = "useOnBlock", at = @At("TAIL"), cancellable = true)
	private void hoeStone(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
		World world = context.getWorld();
		BlockPos blockPos = context.getBlockPos();
		if (this == Items.NETHERITE_HOE) {
			if (context.getSide() != Direction.DOWN && world.getBlockState(blockPos.up()).isAir()) {
				BlockState blockState = SCRAPED_BLOCKS.get(world.getBlockState(blockPos).getBlock());
				if (blockState != null) {
					PlayerEntity playerEntity = context.getPlayer();
					world.playSound(playerEntity, blockPos, SoundEvents.ITEM_HOE_TILL, SoundCategory.BLOCKS, 1.0F, 1.0F);
					if (!world.isClient()) {
						world.setBlockState(blockPos, blockState, 11);
						if (playerEntity != null) {
							context.getStack().damage(1, playerEntity, (p) -> p.sendToolBreakStatus(context.getHand()));
						}
					}
					cir.setReturnValue(ActionResult.success(world.isClient));
				}
			}
			cir.cancel();
		}
	}

	static {
		SCRAPED_BLOCKS = Maps.newHashMap(ImmutableMap.of(Blocks.STONE, SupernaturalCropsScrapedStone.SCRAPED_STONE.getDefaultState()));
	}
}
