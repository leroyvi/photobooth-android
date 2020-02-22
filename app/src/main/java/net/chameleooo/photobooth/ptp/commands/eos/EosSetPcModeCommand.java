/**
 * Copyright 2013 Nils Assbeck, Guersel Ayaz and Michael Zoech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.chameleooo.photobooth.ptp.commands.eos;

import java.nio.ByteBuffer;

import net.chameleooo.photobooth.ptp.EosCamera;
import net.chameleooo.photobooth.ptp.PtpConstants;
import net.chameleooo.photobooth.ptp.PtpCamera;

public class EosSetPcModeCommand extends EosCommand {

    public EosSetPcModeCommand(EosCamera camera) {
        super(camera);
    }

    @Override
    public void exec(PtpCamera.IO io) {
        io.handleCommand(this);
        if (responseCode != PtpConstants.Response.Ok) {
            camera.onPtpError(String.format("Couldn't initialize session! setting PC Mode failed, error code %s",
                    PtpConstants.responseToString(responseCode)));
        }
    }

    @Override
    public void encodeCommand(ByteBuffer b) {
        encodeCommand(b, PtpConstants.Operation.EosSetPCConnectMode, 1);
    }
}
