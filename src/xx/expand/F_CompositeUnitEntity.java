package xx.expand;

import arc.Core;
import arc.input.KeyCode;
import arc.math.Mathf;
import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.entities.Damage;
import mindustry.gen.Player;
import mindustry.gen.UnitEntity;
import mindustry.type.UnitType;
import mindustry.world.blocks.storage.CoreBlock;
import xx.xx_Type.F_CompositeUnitType;

import static mindustry.Vars.player;

public class F_CompositeUnitEntity extends CoreUnitEntity {//恕我本人能力有限，再加上不是很想学java(因为我更喜欢C++)，所以这已经是屎山了，随便看看吧。


    public int
            record2 = 0,//用于增加第一武器的伤害，增加量为部分未成功受到的伤害，即部分抵挡的伤害，或部分rawDamage与受到伤害的差
            sssCounter2 = 0,
            damagedDelayCounter,
            damagedDelay,
            increaseCleanIntervals,
            maxCleanIntervals,
            cleanIntervals ,
            revivesMax,
            revivesSum;
    public float
            seniorHeal_heal,
            deferHeal,
            healthMultiplierCounter,
            recorded,//无需存储
            maxExtraArmorShield = 6000,//要记得，手动写入list。附加装甲
            extraArmorShield = maxExtraArmorShield,
            examineDelay = 30f *60f,//30s
            extraCoreArmor,//用于s-ss
            coreArmor,
            massMultiply = 1f,
            coreShield,
            damageHeal,
            damageHealMultiply,
            chance,
            reviveDelay;

    public float
            recordedRevivesSum,//recorded amount of revives
            recordedHealth,//recorded health
            sssCounter,
            recordedDamage = 0f,
            reviveCounter = 0f;
    public boolean
            hasOtherDamage = false,
            hasHealthMultiplier,
            banConvey = true;
    public double
            healSum = 0,
            rawDamageSum = 0;
    public float mapDiagonal = Mathf.sqrt(Vars.world.width() * Vars.world.width() + Vars.world.height() * Vars.world.height()) * 8f;

    @Override
    public int classId() {
        return EntityRegister.getID(F_CompositeUnitEntity.class);
    }


    @Override
    public void setType(UnitType type) {

        if (this.health <= 0 && this.revivesSum > 0) {
            dead = false;
            this.health = this.maxHealth;
            revivesSum--;
        }//兜底,感觉没什么用

        super.setType(type);

        if (type instanceof F_CompositeUnitType CompositeType) {
            this.revivesMax = this.revivesSum = Math.max(CompositeType.revivesMax, 0);
            this.reviveDelay = Math.max(CompositeType.reviveDelay, 0);
            this.damageHeal = CompositeType.damageHeal;
            this.damageHealMultiply = CompositeType.damageHealMultiply;
            this.maxCleanIntervals = CompositeType.maxCleanIntervals;
            this.increaseCleanIntervals = this.cleanIntervals =  Math.min(Math.max(CompositeType.increaseCleanIntervals, 0), CompositeType.maxCleanIntervals);
            this.coreArmor = CompositeType.coreArmor;
            this.massMultiply = Math.max(CompositeType.massMultiply , 0);
            this.damagedDelay = Math.max(CompositeType.damagedDelay , 0);
            this.recordedHealth = CompositeType.health;
        }
    }
    @Override
    public float mass() {
        return this.hitSize * this.hitSize * (float)Math.PI * massMultiply;
    }




    @Override
    public void update(){


        super.update();
        if(revivesSum<revivesMax) {
            reviveCounter += Time.delta;//Time.delta在60帧为1，30帧下为2。
            if(reviveCounter >= reviveDelay){
                revivesSum++;
                reviveCounter = 0f;
            }
        }
        else if(reviveCounter > 0) reviveCounter -= Time.delta / 10f;
        if(reviveCounter < 0) reviveCounter = 0;

        if(damagedDelayCounter > 0)damagedDelayCounter--;

        if(extraCoreArmor > 0) extraCoreArmor -= Time.delta/60f*8; // 10s耗完

        if(hasHealthMultiplier){
            healthMultiplierCounter += Time.delta;
            healthMultiplier *=2;
            if(healthMultiplierCounter > 600){
                healthMultiplierCounter = 0;
                hasHealthMultiplier = false ;
            }
        }



        if (Core.input.keyTap(KeyCode.k) && banConvey && controller() instanceof Player && player.unit() == this ) teleportToMouse();



        /* self-sustaining system */
        if(sssCounter >= examineDelay) {
            sssCounter -= examineDelay;
            //对比
            double dValue = this.healSum - this.rawDamageSum;
            this.healSum = this.rawDamageSum = 0;
            if(this.recordedHealth + (this.maxHealth * this.recordedRevivesSum) + dValue > this.health + (this.maxHealth * this.revivesSum) + 100f){
                examineDelay =15f * 60f;
                hasOtherDamage = true;
            }
            else examineDelay = 30f *60f;

            //回复损失的血量
            seniorHeal_heal = (float) Math.log( Math.max((this.recordedHealth + this.maxHealth * this.recordedRevivesSum - (this.health + this.maxHealth * this.revivesSum))/ 2000,0) + 1) * 2000;
            this.recordedRevivesSum = this.revivesSum;
            this.recordedHealth = this.health;

            //快速回血
            if(this.revivesSum < this.revivesMax) seniorHeal_heal +=(this.maxHealth - this.health) * 0.2f;

            //给予血量倍率
            hasHealthMultiplier = true;

            //额外护盾
            if(shield < 100000f) shield +=4000f * (revivesMax-revivesSum + 1);

            //增加核心护甲
            extraCoreArmor = 80f;

            //增加附加装甲甲量
            if( extraArmorShield < maxExtraArmorShield) extraArmorShield = Math.min(extraArmorShield + 100 , maxExtraArmorShield);

            //整数倍间隔，增加自适应容量
            sssCounter2 ++;
            if(sssCounter2 >= 2) {
                if(recordedDamage < 10000f) recordedDamage +=400f;
                sssCounter2 = 0;
            }

            //最后统一回复
            this.seniorHeal_heal += deferHeal;
            deferHeal = 0;
            seniorHeal(this.seniorHeal_heal);
            this.seniorHeal_heal = 0;


        }else sssCounter += Time.delta;//拥有许多功能
        /*TODO
           1.回复损失的血量 over
           2.给予血量倍率 over
           3.额外护盾 over
           4.增加场盾
           5.临时增加附加装甲甲量 over
           6.临时增加核心护甲 over
           7.统计受到的伤害，与损失的血量对比，检测是否存在其他形式的伤害 over
           8.存在其他伤害，增强所有效果
           9.整数倍间隔，增加自适应容量 over
           10.存在其他伤害，增加其他伤害数值的核心护盾
           11.休战时，降低部分效果并延长检测时间。脱离休战，立即重置检查时间并减少一次检测时间
           12.调用核心资源
           13.计算受到的DPM，根据条件来改变效果
           14.若血条下降过快，则立即增加计数器
           15.seniorHeal的延迟治疗 over
           16.快速回血 over
           17.这可真多啊，我真的写得出来吗。
           */

    }


    public void seniorHeal(float amount) {
        if(Float.isNaN(amount) || amount <= 0) return;
        float rHealth = this.health;
        float missingHealth = this.maxHealth - this.health;

        if (amount <= missingHealth) {
            this.healSum += amount;
            this.health += amount;
        } else {
            float realHealth = amount + this.health + (this.maxHealth * this.revivesSum);
            int extraRevives = (int) (realHealth / this.maxHealth);

            if(extraRevives > this.revivesMax){
                this.revivesSum = this.revivesMax;
                heal();
                this.deferHeal += realHealth - this.maxHealth;
            }
            else {
                this.revivesSum = extraRevives;
                this.health = realHealth - (extraRevives * this.maxHealth);
                if (this.health == 0){
                    this.revivesSum--;
                    this.health = this.maxHealth;
                }
            }

        }
        this.healSum += this.health - rHealth;
        this.clampHealth();
        if (this.health < this.maxHealth && amount > 0.0F) {
            this.wasHealed = true;
        }
    }//可增加血条，最终溢出部分延迟治疗

    public void healCoreShield(float amount,float limit) {
        if(Float.isNaN(amount) || amount <= 0) return;
        float rHealth = this.health;
        this.health += amount;
        if(this.coreShield < limit){
            this.coreShield += Math.max(this.health - this.maxHealth , 0);
            this.coreShield -= Math.max(this.coreShield - limit , 0);
        }
        this.healSum += this.health - rHealth;
        this.clampHealth();
        if (this.health < this.maxHealth && amount > 0.0F) {
            this.wasHealed = true;
        }
    }//可增加核盾





    @Override
    public void clampHealth() {
        this.healSum -=Math.max(this.health - this.maxHealth , 0);
        this.health = Math.min(this.health, this.maxHealth);
        if (Float.isNaN(this.health)) {
            this.health = 0.0F;
        }
    }
    @Override
    public void heal(float amount) {
        if(Float.isNaN(amount) || amount <= 0) return;
        this.health += amount;
        this.healSum += amount;
        this.clampHealth();
        if (this.health < this.maxHealth && amount > 0.0F) {
            this.wasHealed = true;
        }
    }
    @Override
    public void heal() {
        this.dead = false;
        this.healSum += this.maxHealth - this.health;
        this.health = this.maxHealth;
    }






    private void teleportToMouse() {
        banConvey = false;

        float oldX = x , oldY = y;
        float mouseX = Core.input.mouseWorldX();
        float mouseY = Core.input.mouseWorldY();
        xx_Fx.fxConveyCircle.at(mouseX, mouseY);

        Time.run(5f, () -> {
            if (Core.scene.hasDialog() || Core.scene.hasField() || player == null || player.dead()) {
                banConvey = true;
                return;
            }
            xx_Fx.fxConveyFillCircle.at(oldX,oldY);
            set(mouseX, mouseY);
            vel.setZero();
            Fx.unitControl.at(mouseX, mouseY,0,this);
            xx_Fx.fxConveyCircleB.at(mouseX, mouseY);
            banConvey = true;
        });
    }








    // 伤害it's important. 帧数——>分段——>自适应——>吸收伤害——>护盾——>充能——>核心护甲——>核心护盾——>受回
    @Override
    public void rawDamage(float amount1){
        float amount2 = amount1;

        //帧数限伤
        if(damagedDelayCounter > 0){
            amount2=(float) Math.log(amount1+1);
        }else damagedDelayCounter = damagedDelay;



        //分段限伤
        float reduceLine = (float) 100000 /(revivesMax-revivesSum+1)*2;
        amount2 = amount2 > reduceLine? (float) (reduceLine * Math.log(amount2 / reduceLine) + reduceLine) :amount2;

        if (Float.isNaN(this.health)) {
            this.health = 0.0F;
        }

        //自适应装甲
        if(amount2 >0 ){
            if(amount2 <= recordedDamage){
                recordedDamage -= amount2;
                amount2 *= amount2/( recordedDamage + amount2 );

            }else  recordedDamage = Math.min(recordedDamage + amount1, 10000000);
        }
        this.recordedDamage = (int)recordedDamage;

        //吸收伤害
        record2 += (int) Math.max(amount1 - amount2 , 0);
        record2 = record2 < 0? 2147483647:record2;//我觉得这不可能到达



        boolean
                hasCoreShields = this.coreShield > 1.0E-4F,
                hadShields = this.shield > 1.0E-4F;
        /* TODO 绘制核心护盾的受击动画 */
        if (hadShields) {
            this.shieldAlpha = 1.0F;
        }
        float  shieldDamage = Math.min(Math.max(this.shield, 0.0F), amount2);
        this.shield -= shieldDamage;
        amount2 -= shieldDamage;


        //充能免伤
        if(amount2 <= 0 ) {
            if(cleanIntervals < maxCleanIntervals) cleanIntervals++;
        }
        else if( cleanIntervals > 0){
            cleanIntervals--;
            amount2 = 10 * (float) Math.log(amount2/10 + 1);
        }
        else cleanIntervals += increaseCleanIntervals;

        //core armor
        amount2 -= this.coreArmor + this.extraCoreArmor;
        amount2 = Math.max(amount2,0);


        //core shield
        float coreShieldDamage = Math.min(Math.max(this.coreShield, 0.0F), amount2);
        this.coreShield -= coreShieldDamage;
        amount2 -= coreShieldDamage;
        this.hitTime = 1.0F;


        //受击回复
        amount2 -= Mathf.chance(chance)? damageHeal*damageHealMultiply:damageHeal;
        if(amount2 <= 0) healCoreShield(-amount2,320000);



        amount2 = Math.round( amount2 * 10f) / 10f;
        if (amount2 > 0.0F && this.type.killable) {
            recorded = amount2;
            rawDamageSum += amount2;
            this.health -= amount2;
            if (this.health <= 0.0F && !this.dead ) {

                if(revivesSum > 0){
                    this.healSum += this.maxHealth;
                    this.health = this.maxHealth;
                    this.sssCounter +=15;
                    revivesSum--;
                    if (!isAdded()) add();
                }//多重血条
                else this.kill();

            }

            if (hadShields && this.shield <= 1.0E-4F) {
                Fx.unitShieldBreak.at(this.x, this.y, 0.0F, this.type.shieldColor(this), this);
            }
        }
    }

    @Override
    public void damage(float amount) {
        amount = applyArmorShield(amount);
        this.rawDamage(Damage.applyArmor(amount, this.armorOverride >= 0.0F ? this.armorOverride : this.armor) / this.healthMultiplier / Vars.state.rules.unitHealth(this.team));
    }
    @Override
    public void damageArmorMult(float amount, float armorMult, boolean withEffect) {
        amount = applyArmorShield(amount);
        float pre = this.hitTime;
        this.rawDamage(Damage.applyArmor(amount, this.armorOverride >= 0.0F ? this.armorOverride * armorMult : this.armor * armorMult) / this.healthMultiplier / Vars.state.rules.unitHealth(this.team));
        if (!withEffect) {
            this.hitTime = pre;
        }
    }



    public float applyArmorShield(float amount){
        float absorbed = Math.min(extraArmorShield, amount);
        extraArmorShield -= absorbed;
        return Math.max(amount - absorbed , absorbed * 0.1f);
    }




    @Override
    public void destroy() {
        if (!worldLoadingFlag) {
            if(revivesSum > 0){
                this.health = maxHealth;
                this.healSum +=maxHealth;
                revivesSum--;
            }
            else {
                super.destroy();
                CoreInjector.removeVirtualCore(team.data(), this);
            }
        }
        worldLoadingFlag = false;
    }

    //数据存取
    @Override
    public void write(Writes write) {
        super.write(write);
        write.s(0);
        write.i(revivesMax);
        write.i(revivesSum);
        write.f(reviveDelay);
        write.f(reviveCounter);
        write.bool(banConvey);
        write.i(maxCleanIntervals);
        write.i(cleanIntervals);
        write.f(coreArmor);
        write.f(coreShield);
        write.i(damagedDelayCounter);
        write.f(extraCoreArmor);
        write.f(examineDelay);
        write.i(increaseCleanIntervals);
        write.i(damagedDelay);
        write.f(extraArmorShield);
        write.f(chance);
        write.f(damageHealMultiply);
        write.f(damageHeal);
        write.f(sssCounter);
        write.f(healthMultiplierCounter);
        write.bool(hasHealthMultiplier);
        write.f(deferHeal);
        write.d(healSum);
        write.d(rawDamageSum);
        write.i(sssCounter2);
        write.f(recordedDamage);
        write.f(recordedHealth);
        write.f(recordedRevivesSum);
        write.f(maxExtraArmorShield);
        write.f(massMultiply);
        write.bool(hasOtherDamage);
    }
    @Override
    public void read(Reads read) {
        super.read(read);
        short REV = read.s();//版本控制
        if(REV == 0){
            revivesMax = read.i();
            revivesSum = read.i();
            reviveDelay = read.f();
            reviveCounter = read.f();
            banConvey = read.bool();
            maxCleanIntervals = read.i();
            cleanIntervals = read.i();
            coreArmor = read.f();
            coreShield = read.f();
            damagedDelayCounter = read.i();
            extraCoreArmor = read.f();
            examineDelay = read.f();
            increaseCleanIntervals = read.i();
            damagedDelay = read.i();
            extraArmorShield = read.f();
            chance = read.f();
            damageHealMultiply = read.f();
            damageHeal = read.f();
            sssCounter = read.f();
            healthMultiplierCounter = read.f();
            hasHealthMultiplier = read.bool();
            deferHeal = read.f();
            healSum = read.d();
            rawDamageSum = read.d();
            sssCounter2 = read.i();
            recordedDamage = read.f();
            recordedHealth = read.f();
            recordedRevivesSum = read.f();
            maxExtraArmorShield = read.f();
            massMultiply = read.f();
            hasOtherDamage = read.bool();
        }//怎么这么多啊
    }




}