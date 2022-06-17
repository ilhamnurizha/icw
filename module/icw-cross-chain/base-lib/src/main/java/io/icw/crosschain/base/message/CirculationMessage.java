package io.icw.crosschain.base.message;

import io.icw.base.basic.NulsByteBuffer;
import io.icw.base.basic.NulsOutputStreamBuffer;
import io.icw.crosschain.base.message.base.BaseMessage;
import io.icw.crosschain.base.model.bo.Circulation;
import io.icw.core.exception.NulsException;
import io.icw.core.parse.SerializeUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 链资产发行量消息
 * @author tag
 * @date 2019/4/4
 */
public class CirculationMessage extends BaseMessage {
    private List<Circulation> circulationList;
    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        if(circulationList != null && circulationList.size() > 0){
            for (Circulation circulation:circulationList) {
                stream.writeNulsData(circulation);
            }
        }
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        int course;
        List<Circulation> circulationList = new ArrayList<>();
        while (!byteBuffer.isFinished()) {
            course = byteBuffer.getCursor();
            byteBuffer.setCursor(course);
            circulationList.add(byteBuffer.readNulsData(new Circulation()));
        }
        this.circulationList = circulationList;
    }

    @Override
    public int size() {
        int size = 0;
        if (circulationList != null && circulationList.size() > 0) {
            for (Circulation circulation : circulationList) {
                size +=  SerializeUtils.sizeOfNulsData(circulation);
            }
        }
        return size;
    }

    public List<Circulation> getCirculationList() {
        return circulationList;
    }

    public void setCirculationList(List<Circulation> circulationList) {
        this.circulationList = circulationList;
    }
}
