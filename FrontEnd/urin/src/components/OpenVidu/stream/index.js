/* eslint-disable */
import React, { Component } from "react";
import OvVideoComponent from "./OvVideo";
import "./index.css";

import { IconButton } from "@mui/material";

import MicOffIcon from "@mui/icons-material/MicOff";
import VideocamOffIcon from "@mui/icons-material/VideocamOff";
import VolumeUpIcon from "@mui/icons-material/VolumeUp";
import VolumeOffIcon from "@mui/icons-material/VolumeOff";

export default class StreamComponent extends Component {
  constructor(props) {
    super(props);
    this.state = {
      id: this.props.user.getId(),
      mutedSound: false,
    };
    this.toggleSound = this.toggleSound.bind(this);
  }

  toggleSound() {
    this.setState({ mutedSound: !this.state.mutedSound });
  }

  render() {
    const isInterviewee = this.props.intervieweeId === this.state.id;
    let layout = "col-4";
    if (!this.props.isSomeoneShareScreen && !this.props.intervieweeId) {
      layout = "col-6";
    }
    if (this.props.isSomeoneShareScreen && this.props.user.screenShareActive) {
      layout = "order-first";
    }
    if (!!this.props.intervieweeId && isInterviewee) {
      layout = "order-first";
    }
    return (
      <div className={"video-container p-1 " + layout}>
        <div className="video-wrapper">
          <div className="nickname">
            <span id="nickname">{this.props.user.getNickname()}</span>
          </div>

          {this.props.user !== undefined &&
          this.props.user.getStreamManager() !== undefined ? (
            <>
              <OvVideoComponent
                user={this.props.user}
                isInterviewee={isInterviewee}
                mutedSound={this.state.mutedSound}
              />

              <div>
                {!this.props.user.isLocal() && (
                  <IconButton id="volumeButton" onClick={this.toggleSound}>
                    {this.state.mutedSound ? (
                      <VolumeOffIcon color="secondary" />
                    ) : (
                      <VolumeUpIcon />
                    )}
                  </IconButton>
                )}
              </div>

              <div id="statusIcons">
                {!this.props.user.isVideoActive() ? (
                  <div id="camIcon">
                    <VideocamOffIcon id="statusCam" />
                  </div>
                ) : null}

                {!this.props.user.isAudioActive() ? (
                  <div id="micIcon">
                    <MicOffIcon id="statusMic" />
                  </div>
                ) : null}
              </div>
            </>
          ) : null}
        </div>
      </div>
    );
  }
}