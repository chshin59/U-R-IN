package com.dongpop.urin.domain.meeting.service;

import com.dongpop.urin.domain.meeting.dto.request.MeetingCreateDto;
import com.dongpop.urin.domain.meeting.dto.response.MeetingIdDto;
import com.dongpop.urin.domain.meeting.entity.Meeting;
import com.dongpop.urin.domain.meeting.repository.MeetingRepository;
import com.dongpop.urin.domain.member.entity.Member;
import com.dongpop.urin.domain.study.entity.Study;
import com.dongpop.urin.domain.study.repository.StudyRepository;
import com.dongpop.urin.global.error.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.transaction.Transactional;
import java.util.UUID;

import static com.dongpop.urin.global.error.errorcode.MeetingErrorCode.*;
import static com.dongpop.urin.global.error.errorcode.StudyErrorCode.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class MeetingService {

    private final StudyRepository studyRepository;
    private final MeetingRepository meetingRepository;

    @Transactional
    public String issueSessionId(Member member, int studyId) {
        Study study = getStudy(studyId);

        if (isStudyLeader(member, study)) {
            String sessionId = UUID.randomUUID().toString();
            study.saveSessionId(sessionId);
            return sessionId;
        }

        if (!StringUtils.hasText(study.getSessionId())) {
            throw new CustomException(SESSIONID_IS_NOT_EXIST);
        }
        return study.getSessionId();
    }

    @Transactional
    public MeetingIdDto createMeeting(int studyId, Member member, MeetingCreateDto meetingCreateDto) {
        Study study = getStudy(studyId);
        if (!isStudyLeader(member, study)) {
            throw new CustomException(ONLY_POSSIBLE_STUDY_LEADER);
        }
        if (!meetingCreateDto.getSessionId().equals(study.getSessionId())) {
            throw new CustomException(SESSIONID_IS_NOT_VALID);
        }
        if (study.getIsOnair()) {
            throw new CustomException(MEETING_IS_ALREADY_ONAIR);
        }

        study.changeOnairStatus(meetingCreateDto.getIsConnected());
        int meetingId = meetingCreateDto.getIsConnected() ?
                meetingRepository.save(new Meeting(study)).getId() : 0;
        return new MeetingIdDto(meetingId);
    }

    @Transactional
    public void endMeeting(int studyId, int meetingId, Member member) {
        Study study = getStudy(studyId);
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new CustomException(MEETING_IS_NOT_EXIST));

        if (study.getId() != meeting.getStudy().getId()) {
            throw new CustomException(MEETING_IS_NOT_PART_OF_STUDY);
        }
        if (study.getStudyLeader().getId() != member.getId()) {
            throw new CustomException(ONLY_POSSIBLE_STUDY_LEADER);
        }

        study.closeMeeting();
        meeting.closeMeeting();
    }

    private Study getStudy(int studyId) {
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new CustomException(STUDY_DOES_NOT_EXIST));
        return study;
    }

    private boolean isStudyLeader(Member member, Study study) {
        return study.getStudyLeader().getId() == member.getId();
    }


}