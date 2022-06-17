/*
 * *
 *  * MIT License
 *  *
 *  * Copyright (c) 2017-2019 nuls.io
 *  *
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the "Software"), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  *
 *  * The above copyright notice and this permission notice shall be included in all
 *  * copies or substantial portions of the Software.
 *  *
 *  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  * SOFTWARE.
 *
 */
package io.icw.round;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import io.icw.base.data.Address;
import io.icw.core.rpc.model.ApiModel;
import io.icw.core.rpc.model.ApiModelProperty;
import io.icw.core.rpc.model.TypeDescriptor;
import io.icw.core.rpc.util.NulsDateUtils;

@ApiModel(name = "轮次信息")
public class MeetingRound {
    /**
     * 总权重
     * Total weight
     * */
    @ApiModelProperty(description = "当前轮次总权重")
    private double totalWeight;
    /**
     * 本地打包节点在当前轮次的下标
     * Subscription of Local Packing Node in Current Round
     * */
    @ApiModelProperty(description = "轮次下标")
    private long index;
    /**
     * 当前轮次开始打包时间
     * Current Round Start Packing Time
     * */
    @ApiModelProperty(description = "轮次开始时间")
    private long startTime;
    /**
     * 当前轮次打包结束时间
     * End time of front packing
     * */
    @ApiModelProperty(description = "轮次结束时间")
    private long endTime;
    /**
     * 当前轮次打包节点数量
     * Number of Packing Nodes in Current Round
     * */
    @ApiModelProperty(description = "本轮次出块节点数")
    private int memberCount;
    /**
     * 当前轮次打包成员列表
     * Current rounds packaged membership list
     * */
    @ApiModelProperty(description = "本轮次出块成员信息", type = @TypeDescriptor(value = List.class, collectionElement = MeetingMember.class))
    private List<MeetingMember> memberList;
    /**
     * 上一轮轮次信息
     * Last round of information
     * */
    @ApiModelProperty(description = "上一轮信息")
    private MeetingRound preRound;
    /**
     * 本地打包成员信息
     * Locally packaged member information
     * */
    @ApiModelProperty(description = "当前节点出块信息")
    private MeetingMember myMember;
    private List<MeetingMember> myMemberList = new ArrayList<>();

    public MeetingRound getPreRound() {
        return preRound;
    }

    public void setPreRound(MeetingRound preRound) {
        this.preRound = preRound;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public int getMemberCount() {
        return memberCount;
    }

    /**
     * 根据节点地址获取节点对应的打包信息
     * Get the packing information corresponding to the node according to the address of the node
     */
    public MeetingMember getMemberByAgentAddress(byte[] address) {
        for (MeetingMember member : memberList) {
            if (Arrays.equals(address, member.getAgent().getAgentAddress())) {
                return member;
            }
        }
        return null;
    }

    public long getIndex() {
        return index;
    }

    public void setIndex(long index) {
        this.index = index;
    }


    public double getTotalWeight() {
        return totalWeight;
    }

    public List<MeetingMember> getMemberList() {
        return memberList;
    }

    public MeetingMember getMyMember() {
        return myMember;
    }

    public List<MeetingMember> getLocalMembers(){
        return myMemberList;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        for (MeetingMember member : this.getMemberList()) {
            str.append(Address.fromHashs(member.getAgent().getPackingAddress()).getBase58());
            str.append(" ,order:" + member.getPackingIndexOfRound());
            str.append(",packTime:" + new Date(member.getPackEndTime() * 1000));
            str.append(",creditVal:" + member.getAgent().getRealCreditVal());
            str.append("\n");
        }
        if (null == this.getPreRound()) {
            return ("round:index:" + this.getIndex() + " , start:" + new Date(this.getStartTime() * 1000)
                    + ", netTime:(" + new Date(NulsDateUtils.getCurrentTimeMillis()).toString() + ") , totalWeight : " + totalWeight + " ,members:\n" + str);
        } else {
            return ("round:index:" + this.getIndex() + " ,preIndex:" + this.getPreRound().getIndex() + " , start:" + new Date(this.getStartTime())
                    + ", netTime:(" + new Date(NulsDateUtils.getCurrentTimeMillis()).toString() + ") , totalWeight : " + totalWeight + "  , members:\n" + str);
        }
    }
}
